This repository demonstrates the utilization of the [MigratoryData Java API](https://migratorydata.com/docs/client-api/java/getting_started/) with the [JWT add-on](https://migratorydata.com/docs/integrations/authorization-jwt/), which is available with the [MigratoryData Server](https://migratorydata.com/downloads/migratorydata-6/).

To establish a connection with the MigratoryData Server, the client generates a JWT token by initiating a request to the backend configured at the `backendEndpoint` parameter in the `Main` file. The generated JWT token grants the client `publish` and `subscribe` permissions for the `/server/status` subject, which is utilized to publish and subscribe a message every five seconds to the same subject.

As the JWT token approaches its expiration, the API is notified with `TOKEN_TO_EXPIRE`, prompting the client to generate a new token and update the server with the new token. The client continues to publish and receive messages using the updated token.

Further information on configuring the JWT add-on can be found on both the [official documentation](https://migratorydata.com/docs/integrations/authorization-jwt/) and a related [blog post](https://migratorydata.com/blog/migratorydata-jwt-auth/).


To run the example run the following command:

```bash
gradle run
```