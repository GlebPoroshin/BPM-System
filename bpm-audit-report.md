### 1) Сводная таблица (обязательная)
| Сервис | Бизнес-задача (1 предложение) | Входы (REST/events/jobs) | Выходы (calls/events/db writes) | Данные (таблицы/модели/миграции) | С кем общается | Улики (пути к файлам) |
| --- | --- | --- | --- | --- | --- | --- |
| bpm-main-service | Запускает процессы найма/увольнения и предоставляет REST/GraphQL интерфейс управления; онбординг инициирует через gRPC. | REST: `/api/employees`, `/api/employees/dismissals`, `/api/onboarding/{onboardingId}`, `/api/onboarding/{onboardingId}/tasks`; GraphQL: `/graphql`. | RabbitMQ fanout: `bpm-employee-created`, `bpm-employee-dismissed`; gRPC вызовы OnboardingService; запись/удаление в InMemoryStorage. | InMemoryStorage: departments/teams/employees. | REST/GraphQL клиенты; bpm-onboarding-service (gRPC); RabbitMQ -> bpm-audit-service/bpm-compliance-service/bpm-statistics-service/notification-service. | `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/EmployeeService.kt` — публикация событий и gRPC; `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/EmployeeApi.kt` — REST эндпоинты; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/graphql/EmployeeDataFetcher.kt` — GraphQL; `bpm-main-service/src/main/resources/application.properties` — порт, RabbitMQ, gRPC. |
| bpm-onboarding-service | Ведёт онбординг сотрудников: создаёт запись и задачи, возвращает статус, обновляет задачи по gRPC. | gRPC: CreateOnboarding, GetOnboardingStatus, UpdateOnboardingTask. | gRPC ответы; обновление in-memory записей. | ConcurrentHashMap onboardingRecords (OnboardingRecord). | bpm-main-service (gRPC). | `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` — gRPC методы; `bpm-grpc-contracts/src/main/proto/onboarding-service.proto` — контракт; `bpm-onboarding-service/src/main/resources/application.properties` — порты. |
| bpm-compliance-service | Отслеживает комплаенс-задачи по найму/увольнению и отдаёт сводки/управление задачами по REST. | RabbitMQ: EmployeeCreatedEvent, EmployeeDismissedEvent; REST: `/compliance/summary`, `/compliance/records`, `/compliance/{employeeId}/tasks/complete`. | REST ответы; обновление ComplianceRecord; ack/nack и DLQ. | ComplianceRecord в ConcurrentHashMap. | RabbitMQ; REST клиенты. | `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt` — очереди/обменники; `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/service/ComplianceService.kt` — записи; `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/controller/ComplianceController.kt` — REST. |
| bpm-audit-service | Аудитирует события найма/увольнения через логирование. | RabbitMQ: EmployeeCreatedEvent, EmployeeDismissedEvent; DLQ. | Логи; ack/nack. | Наборы дедупликации в памяти. | RabbitMQ. | `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` — обработка событий и DLQ. |
| bpm-statistics-service | Собирает статистику по созданным сотрудникам и агрегирует по отделам. | RabbitMQ: EmployeeCreatedEvent. | Логи со статистикой; обновление in-memory статистики; DLQ. | EmployeeStatistic в ConcurrentHashMap. | RabbitMQ. | `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/listener/EmployeeStatisticsListener.kt` — очередь; `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/service/StatisticsService.kt` — агрегации. |
| notification-service | Доставляет уведомления о найме/увольнении через WebSocket; поддерживает ручную рассылку через REST. | RabbitMQ: EmployeeCreatedEvent, EmployeeDismissedEvent; REST: `/api/notifications/broadcast`, `/api/notifications/unicast`, `/api/notifications/stats`; WebSocket: `/ws/notifications`. | WebSocket сообщения; REST ответы; ack/nack и DLQ. | WebSocket сессии в памяти. | RabbitMQ; WebSocket/REST клиенты. | `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` — события; `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/config/WebSocketConfig.kt` — WS путь; `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/controller/NotificationController.kt` — REST. |

### 2) Карточки сервисов (обязательные)

**Сервис bpm-main-service**  
Бизнес-задача: Запускает процессы найма и увольнения сотрудников и предоставляет REST/GraphQL интерфейс управления; статусы онбординга получает через gRPC (`bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/EmployeeApi.kt` — описания процессов; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/EmployeeService.kt` — создание/увольнение и публикация событий; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/controller/OnboardingController.kt` — REST прокси к gRPC; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/graphql/EmployeeDataFetcher.kt` — GraphQL).  
Чем занимается сейчас:
- Принимает `POST /api/employees` и инициирует найм ( `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/EmployeeApi.kt` — @PostMapping; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/controller/EmployeeController.kt` — реализация).
- Принимает `POST /api/employees/dismissals` и инициирует увольнение ( `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/EmployeeApi.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/controller/EmployeeController.kt` ).
- Отдаёт `GET /api/employees/{id}` и `GET /api/employees` ( `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/EmployeeApi.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/controller/EmployeeController.kt` ).
- Вызывает gRPC CreateOnboarding при найме ( `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/EmployeeService.kt` — createOnboarding; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/OnboardingGrpcClient.kt` ).
- Проксирует статус онбординга по REST через gRPC ( `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/OnboardingApi.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/controller/OnboardingController.kt` ).
- Публикует EmployeeCreatedEvent и EmployeeDismissedEvent в RabbitMQ fanout ( `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/EmployeeService.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/config/RabbitMQConfig.kt` ).
- Хранит сотрудников/отделы/команды в InMemoryStorage и инициализирует справочники ( `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/storage/InMemoryStorage.kt` ).
- Обслуживает GraphQL запросы и мутации ( `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/graphql/EmployeeDataFetcher.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/graphql/DepartmentDataFetcher.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/graphql/TeamDataFetcher.kt` ).
- Публикует root-эндпоинт `/api` с ссылками на Swagger/GraphQL/GraphiQL ( `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/controller/RootController.kt` ).
API:
- HTTP порт 8080 ( `bpm-main-service/src/main/resources/application.properties` — server.port=8080 ).
- REST `/api/employees`, `/api/employees/dismissals`, `/api/employees/{id}`, `/api/employees` ( `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/EmployeeApi.kt` ).
- REST `/api/onboarding/{onboardingId}`, `/api/onboarding/{onboardingId}/tasks` ( `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/OnboardingApi.kt` ).
- GraphQL `/graphql`, схемы Query/Mutation ( `bpm-api/src/main/resources/graphql-client/schema.graphqls`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/graphql/EmployeeDataFetcher.kt` ).
- Swagger UI `/swagger-ui.html` и ссылки из root ( `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/controller/RootController.kt`; `bpm-main-service/src/main/resources/application.properties` — springdoc paths ).
События:
- Производит EmployeeCreatedEvent -> exchange `bpm-employee-created` (fanout) ( `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/EmployeeService.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/config/RabbitMQConfig.kt` ).
- Производит EmployeeDismissedEvent -> exchange `bpm-employee-dismissed` (fanout) ( `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/EmployeeService.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/config/RabbitMQConfig.kt` ).
Данные:
- InMemoryStorage: employees/teams/departments, employeeSequence ( `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/storage/InMemoryStorage.kt` ).
Интеграции:
- RabbitMQ (RabbitTemplate) ( `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/EmployeeService.kt` ).
- gRPC клиент к onboarding-service ( `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/OnboardingGrpcClient.kt`; `bpm-main-service/src/main/resources/application.properties` — grpc.client.onboarding-service.address ).
Улики:
- `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/BpmMainServiceApplication.kt` — SpringBootApplication и исключение DataSource.
- `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/EmployeeApi.kt` — REST контракты и описания процессов.
- `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/EmployeeService.kt` — бизнес-логика и события.
- `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/controller/OnboardingController.kt` — REST -> gRPC.
- `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/graphql/EmployeeDataFetcher.kt` — GraphQL.
- `bpm-main-service/src/main/resources/application.properties` — конфигурация портов и интеграций.

**Сервис bpm-onboarding-service**  
Бизнес-задача: Управляет онбордингом сотрудников через gRPC: создаёт запись и задачи, возвращает статус и обновляет задачи (`bpm-grpc-contracts/src/main/proto/onboarding-service.proto` — методы; `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` — реализация).  
Чем занимается сейчас:
- Обрабатывает gRPC CreateOnboarding и создаёт OnboardingRecord ( `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` ).
- Генерирует дефолтные задачи по позиции ( `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` — generateDefaultTasks ).
- Сохраняет записи онбординга в памяти ( `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` — onboardingRecords ).
- Отдаёт статус по gRPC GetOnboardingStatus ( `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` ).
- Обновляет задачу и статус по gRPC UpdateOnboardingTask ( `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` ).
API:
- gRPC методы CreateOnboarding/GetOnboardingStatus/UpdateOnboardingTask ( `bpm-grpc-contracts/src/main/proto/onboarding-service.proto` ).
- gRPC порт 9091 и HTTP порт 8084 ( `bpm-onboarding-service/src/main/resources/application.properties` — grpc.server.port, server.port ).
События:
- Не публикует и не потребляет события (отсутствуют @RabbitListener/producer в модуле).
Данные:
- In-memory ConcurrentHashMap onboardingRecords ( `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` ).
Интеграции:
- gRPC сервер для bpm-main-service ( `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` ).
Улики:
- `bpm-grpc-contracts/src/main/proto/onboarding-service.proto` — gRPC контракт.
- `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` — логика онбординга.
- `bpm-onboarding-service/src/main/resources/application.properties` — порты.

**Сервис bpm-compliance-service**  
Бизнес-задача: Ведёт комплаенс-процесс для найма и увольнения, хранит задачи/статусы и даёт REST доступ к сводке и управлению задачами (`bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/service/ComplianceService.kt` — записи и задачи; `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/controller/ComplianceController.kt` — REST).  
Чем занимается сейчас:
- Слушает EmployeeCreatedEvent из exchange `bpm-employee-created` ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt` ).
- Создаёт ComplianceRecord и дефолтные задачи при найме ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/service/ComplianceService.kt` — registerOnboarding, defaultTasks ).
- Слушает EmployeeDismissedEvent и закрывает задачи ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt`; `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/service/ComplianceService.kt` — registerDismissal ).
- Обрабатывает DLQ через `dlx-exchange` ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt` ).
- Отдаёт сводку `/compliance/summary` ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/controller/ComplianceController.kt` ).
- Отдаёт список записей `/compliance/records` ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/controller/ComplianceController.kt` ).
- Позволяет закрывать задачу `/compliance/{employeeId}/tasks/complete` ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/controller/ComplianceController.kt` ).
API:
- HTTP порт 8085 ( `bpm-compliance-service/src/main/resources/application.properties` — server.port=8085 ).
- REST `/compliance/summary`, `/compliance/records`, `/compliance/{employeeId}/tasks/complete` ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/controller/ComplianceController.kt` ).
События:
- Потребляет EmployeeCreatedEvent из exchange `bpm-employee-created` в queue `compliance-employee-queue` ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt` ).
- Потребляет EmployeeDismissedEvent из exchange `bpm-employee-dismissed` в queue `compliance-employee-dismissed-queue` ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt` ).
- DLQ: `compliance-queue.dlq` через `dlx-exchange` ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt` ).
Данные:
- ComplianceRecord и задачи в ConcurrentHashMap ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/service/ComplianceService.kt` ).
Интеграции:
- RabbitMQ ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt` ).
Улики:
- `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt` — очереди и обменники.
- `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/service/ComplianceService.kt` — комплаенс-логика.
- `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/controller/ComplianceController.kt` — REST.

**Сервис bpm-audit-service**  
Бизнес-задача: Аудитирует события найма/увольнения путём логирования (`bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` — логирование событий).  
Чем занимается сейчас:
- Слушает EmployeeCreatedEvent из `bpm-employee-created` ( `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` ).
- Логирует создание сотрудника ( `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` — log.info AUDIT ).
- Слушает EmployeeDismissedEvent из `bpm-employee-dismissed` ( `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` ).
- Логирует увольнение сотрудника ( `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` ).
- Обрабатывает DLQ `audit-employee-queue.dlq` ( `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` ).
API:
- HTTP порт 8082 ( `bpm-audit-service/src/main/resources/application.properties` — server.port=8082 ); публичных REST эндпоинтов в коде нет.
События:
- Потребляет EmployeeCreatedEvent в queue `audit-employee-queue` ( `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` ).
- Потребляет EmployeeDismissedEvent в queue `audit-employee-dismissed-queue` ( `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` ).
- DLQ: `audit-employee-queue.dlq` через `dlx-exchange` ( `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` ).
Данные:
- Наборы processedEmployeeCreations/processedEmployeeDismissals в памяти ( `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` ).
Интеграции:
- RabbitMQ ( `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` ).
Улики:
- `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt` — обработка событий и DLQ.
- `bpm-audit-service/src/main/resources/application.properties` — порт.

**Сервис bpm-statistics-service**  
Бизнес-задача: Собирает статистику по созданным сотрудникам и агрегирует по отделам (`bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/service/StatisticsService.kt` — агрегации; `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/listener/EmployeeStatisticsListener.kt` — источник событий).  
Чем занимается сейчас:
- Слушает EmployeeCreatedEvent из `bpm-employee-created` ( `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/listener/EmployeeStatisticsListener.kt` ).
- Записывает статистику по сотруднику ( `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/service/StatisticsService.kt` — recordEmployeeCreation ).
- Логирует общий счётчик и распределение по отделам ( `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/service/StatisticsService.kt` — log.info ).
- Хранит статистику в ConcurrentHashMap ( `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/service/StatisticsService.kt` ).
- Обрабатывает DLQ `statistics-employee-created-queue.dlq` ( `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/listener/EmployeeStatisticsListener.kt` ).
API:
- HTTP порт 8083 ( `bpm-statistics-service/src/main/resources/application.properties` — server.port=8083 ); публичных REST эндпоинтов в коде нет.
События:
- Потребляет EmployeeCreatedEvent в queue `statistics-employee-created-queue` ( `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/listener/EmployeeStatisticsListener.kt` ).
- DLQ: `statistics-employee-created-queue.dlq` через `dlx-exchange` ( `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/listener/EmployeeStatisticsListener.kt` ).
Данные:
- EmployeeStatistic в ConcurrentHashMap ( `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/service/StatisticsService.kt` ).
Интеграции:
- RabbitMQ ( `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/listener/EmployeeStatisticsListener.kt` ).
Улики:
- `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/listener/EmployeeStatisticsListener.kt` — обработка событий.
- `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/service/StatisticsService.kt` — хранение и агрегации.

**Сервис notification-service**  
Бизнес-задача: Рассылает уведомления о найме/увольнении по WebSocket и поддерживает ручную рассылку по REST (`notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` — уведомления; `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/controller/NotificationController.kt` — REST).  
Чем занимается сейчас:
- Слушает EmployeeCreatedEvent и рассылает broadcast + unicast ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` ).
- Слушает EmployeeDismissedEvent и рассылает broadcast + unicast ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` ).
- Пишет уведомления в WebSocket сессии ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/handler/NotificationWebSocketHandler.kt` — broadcast/sendToUser ).
- Обрабатывает DLQ `notification-queue.dlq` ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` ).
- Принимает REST `POST /api/notifications/broadcast` ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/controller/NotificationController.kt` ).
- Принимает REST `POST /api/notifications/unicast` ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/controller/NotificationController.kt` ).
- Отдаёт REST `GET /api/notifications/stats` ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/controller/NotificationController.kt` ).
- Поднимает WebSocket `/ws/notifications` и отслеживает активные сессии ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/config/WebSocketConfig.kt`; `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/handler/NotificationWebSocketHandler.kt` ).
API:
- HTTP порт 8086 ( `notification-service/src/main/resources/application.properties` — server.port=8086 ).
- REST `/api/notifications/broadcast`, `/api/notifications/unicast`, `/api/notifications/stats` ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/controller/NotificationController.kt` ).
- WebSocket `/ws/notifications` ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/config/WebSocketConfig.kt` ).
События:
- Потребляет EmployeeCreatedEvent в queue `notification-employee-created-queue` ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` ).
- Потребляет EmployeeDismissedEvent в queue `notification-employee-dismissed-queue` ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` ).
- DLQ: `notification-queue.dlq` через `dlx-exchange` ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` ).
Данные:
- WebSocket сессии и привязка userId в памяти ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/handler/NotificationWebSocketHandler.kt` ).
Интеграции:
- RabbitMQ ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` ).
- WebSocket клиенты ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/config/WebSocketConfig.kt` ).
Улики:
- `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` — обработка событий.
- `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/handler/NotificationWebSocketHandler.kt` — рассылка в WS.
- `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/controller/NotificationController.kt` — REST.

### 3) Карта взаимодействий

Диаграмма (ASCII):
```
[REST/GraphQL клиенты] -> bpm-main-service -> (gRPC) -> bpm-onboarding-service
bpm-main-service -> RabbitMQ (bpm-employee-created) -> bpm-compliance-service
bpm-main-service -> RabbitMQ (bpm-employee-created) -> bpm-statistics-service
bpm-main-service -> RabbitMQ (bpm-employee-created) -> bpm-audit-service
bpm-main-service -> RabbitMQ (bpm-employee-created) -> notification-service -> [WebSocket клиенты]
bpm-main-service -> RabbitMQ (bpm-employee-dismissed) -> bpm-compliance-service
bpm-main-service -> RabbitMQ (bpm-employee-dismissed) -> bpm-audit-service
bpm-main-service -> RabbitMQ (bpm-employee-dismissed) -> notification-service
[REST клиенты] -> bpm-compliance-service
[REST клиенты] -> notification-service
```

Контракты и общие модули:
- `bpm-events-contracts/src/main/kotlin/com/rut/glebporoshin/sop/events/EmployeeCreatedEvent.kt` и `bpm-events-contracts/src/main/kotlin/com/rut/glebporoshin/sop/events/EmployeeDismissedEvent.kt` — типы событий для RabbitMQ.
- `bpm-grpc-contracts/src/main/proto/onboarding-service.proto` — gRPC контракт OnboardingService.
- `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/EmployeeApi.kt` и `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/OnboardingApi.kt` — REST контракты.
- `bpm-api/src/main/resources/graphql-client/schema.graphqls` — GraphQL схема.
- `bpm-main-service/build.gradle.kts` — зависимости на bpm-api/bpm-events-contracts/bpm-grpc-contracts.

Ключевые потоки:
- Найм сотрудника: REST `POST /api/employees` -> создание сотрудника и gRPC CreateOnboarding -> публикация EmployeeCreatedEvent -> обработка в compliance/statistics/audit/notification ( `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/EmployeeApi.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/EmployeeService.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/OnboardingGrpcClient.kt`; `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt`; `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt`; `bpm-statistics-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_statistics_service/listener/EmployeeStatisticsListener.kt`; `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt`; `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` ).
- Увольнение сотрудника: REST `POST /api/employees/dismissals` -> публикация EmployeeDismissedEvent -> обработка в compliance/audit/notification ( `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/EmployeeApi.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/EmployeeService.kt`; `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt`; `bpm-audit-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_audit_service/listener/EmployeeEventListener.kt`; `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt` ).
- Получение статуса онбординга: REST `GET /api/onboarding/{onboardingId}` -> gRPC GetOnboardingStatus ( `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/OnboardingApi.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/controller/OnboardingController.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/service/OnboardingGrpcClient.kt`; `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` ).
- Обновление задачи онбординга: REST `PUT /api/onboarding/{onboardingId}/tasks` -> gRPC UpdateOnboardingTask -> пересчёт статуса ( `bpm-api/src/main/kotlin/com/rut/glebporoshin/sop/bpmapi/api/endpoints/OnboardingApi.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/controller/OnboardingController.kt`; `bpm-onboarding-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_onboarding_service/service/OnboardingServiceImpl.kt` ).
- GraphQL чтение/мутации: `/graphql` -> DGS DataFetchers -> InMemoryStorage ( `bpm-api/src/main/resources/graphql-client/schema.graphqls`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/graphql/EmployeeDataFetcher.kt`; `bpm-main-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_main_service/storage/InMemoryStorage.kt` ).
- Сводка комплаенса: REST `/compliance/summary` -> агрегирование по записям, созданным из событий ( `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/controller/ComplianceController.kt`; `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/service/ComplianceService.kt`; `bpm-compliance-service/src/main/kotlin/com/rut/glebporoshin/sop/bpm_compliance_service/listener/ComplianceEventListener.kt` ).
- Ручная рассылка уведомлений: REST `/api/notifications/broadcast` или `/api/notifications/unicast` -> отправка в WebSocket сессии ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/controller/NotificationController.kt`; `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/handler/NotificationWebSocketHandler.kt` ).
- Автоуведомления по событиям: EmployeeCreatedEvent/EmployeeDismissedEvent -> NotificationEventListener -> WebSocket broadcast/unicast ( `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/listener/NotificationEventListener.kt`; `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/handler/NotificationWebSocketHandler.kt`; `notification-service/src/main/kotlin/com/rut/glebporoshin/sop/notification_service/config/WebSocketConfig.kt` ).

### 4) Вопросы (только P0)
- Вопросов P0 нет.
