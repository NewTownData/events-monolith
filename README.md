# Events Monolith

Description of the architecture is at https://www.newtowndata.com/blog/2023-10/monolithic-event-based-system-blueprint.html

## Pre-requisites

- [Java 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
- [Apache Maven](https://maven.apache.org/install.html)
- [Node.js LTS](https://nodejs.org/en/download/)
- [AWS CDK v2](https://docs.aws.amazon.com/cdk/v2/guide/getting_started.html#getting_started_install)
- AWS account that you can deploy to

## How to run locally

1. Go to `app` (in the project root).
1. Run `mvn exec:java "-Dexec.args=example-event.json"`

## How to deploy to AWS

1. Go to `app` (in the project root).
1. Run `mvn clean package`.
1. Go to `infrastructure` (in the project root).
1. Define environment variable `STACK_PREFIX` with a unique value, which is needed for AWS S3 bucket creation that must have a globally unique name.
1. Run `cdk diff`.
1. Run `cdk deploy` if comfortable with the above.
1. Send a new message to the SQS queue with name ending on `events-monolith-app-queue`. An example event is at `app/test-event.json`.
1. Observe the execution in CloudWatch logs or AWS X-ray traces.

## CDK clean up

This project contains [CDK clean up](infrastructure/docs/clean-up.md) code that can be used for cleaning up unused CDK assets in a staging bucket.

## License

Apache License, Version 2.0. See [LICENSE](LICENSE) for more details.
