# Group 4 SWP391

## 1. Project Structure

```text
HotelPage/
│
├── nb-configuration.xml
├── pom.xml
│
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── controller/
    │   │   ├── dao/
    │   │   ├── db/
    │   │   ├── filter/
    │   │   ├── model/
    │   │   └── util/
    │   │
    │   └── webapp/
    │       ├── assets/
    │       │   ├── css/
    │       │   ├── icons/
    │       │   └── img/
    │       ├── META-INF/
    │       ├── script/
    │       └── WEB-INF/
    │           ├── booking/
    │           ├── customer/
    │           ├── details/
    │           ├── discount/
    │           ├── error/
    │           ├── history/
    │           ├── include/
    │           ├── login/
    │           ├── pay/
    │           ├── profile/
    │           ├── register/
    │           ├── reports/
    │           ├── room/
    │           ├── roomType/
    │           └── web.xml
    │
    └── test/
        └── java/
```

## 2. Database

```text
HotelDB.sql
```

## 3. Main Components

- Controller: Handle HTTP requests and application flow
- DAO: Handle database access
- Model: Represent application data
- Filter: Handle authentication
- JSP Views: Provide web pages
- Assets: CSS, JavaScript, icons and images
