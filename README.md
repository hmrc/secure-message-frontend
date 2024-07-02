# secure-message-frontend

## Run the project locally 

`sbt "run 9055 -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"`

## Run the tests and sbt fmt before raising a PR

Ensure you have service-manager python environment setup:

`source ../servicemanager/bin/activate`

Format:

`sbt fmt`

Then run the tests and coverage report:

`sbt clean coverage test coverageReport`

If your build fails due to poor testing coverage, *DO NOT* lower the test coverage, instead inspect the generated report located here on your local repo: `/target/scala-2.12/scoverage-report/index.html`

Then run the integration tests:

## Integration Tests
`sm2 --start DC_SECURE_MESSAGE_FRONTEND_IT`

`sbt it / test`

`sm2 --stop DC_SECURE_MESSAGE_FRONTEND_IT`

## Service endpoints
 `/:clientService/messages`                                          

  `/messages/count`                                        

  `/:clientService/messages/:id`                                     

  `/:clientService/messages/result`

## Swagger endpoint

Available locally here: http://localhost:9055/secure-message-frontend/api/schema.json

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

