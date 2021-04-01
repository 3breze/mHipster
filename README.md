## mHipster aka CRUD Generator


Define domain classes, annotate them with @Entity, describe your entity relations (@OneToOne, @OneToMany etc.) and let mHipster do the rest.

`mvn mHipster:gen`

The result is fully funcitonal multi-layer Create, Read, Update, Delete application, starting from API (presentational) layer, DTO Objects (Request and Response), Service layer and finally DAO layer.


API layer will use Request/Response DTO objects, service layer will be generated based on entity relations and DAO layer will use QueryDSL. 





