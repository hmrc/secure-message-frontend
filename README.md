# secure-message-frontend

# Overview
Secure message frontend is a microservice to allow other services to integrate with secure messages inbox & retrieve the message partials.

- [Digital Contact Runbook](https://confluence.tools.tax.service.gov.uk/display/DCT/Digital+Contact+Runbook)
- [Digital Contact Confluence home page](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?spaceKey=DCT&title=Digital+Contact)
- [Digital Contact Slack channel - #team-digital-contact](https://hmrcdigital.slack.com/archives/C0J85LC3W)

# Integration

## Service endpoints

 Path                                   | Supported Methods | Description                                                             |
|---------------------------------------| ----------------  |-------------------------------------------------------------------------|
| ```/:clientService/messages```        | GET               | returns HTML partial with list of messages.                             |
| ```/:clientService/messages/:id```    | GET               | returns HTML partial with list of messages for a given conversation id. |
| ```/:clientService/messages/result``` | GET               | returns HTML partial with list of messages in the conversation.         |
| ```/messages/count ```                | GET               | returns JSON containing the message count.                              |

# Message frontend endpoints

 Path                            | Supported Methods | Description                                                                        |
| ------------------------------ | ----------------  |------------------------------------------------------------------------------------|
| ```/messages```                | GET               | returns HTML partial with list of messages for current authenticated user.         |
| ```/messages/bta```            | GET               | returns HTML partial with list of messages for current authenticated user for BTA. |
| ```/messages/:encryptedUrl ``` | GET               | returns HTML Partial containing the formatted message                              |
| ```/messages/inbox-link ```    | GET               | returns HTML Partial containing the formatted message count                        |
| ```/messages/count ```         | GET               | returns JSON containing the message count                                          |

## Integration Testing
`sm2 --start DC_SECURE_MESSAGE_FRONTEND_IT`
`sbt it / test`
`sm2 --stop DC_SECURE_MESSAGE_FRONTEND_IT`

## Run the project locally

`sbt "run 9055 -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"`

## SBT tasks
```bash
# Format the code
sbt fmt

# Clean, build test and integration test
sbt clean test it/test

# Run a coverage report
sbt clean coverage test coverageReport
```

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").