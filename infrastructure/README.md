# Welcome to your CDK Java project!

Events monolith infrastructure project uses Java-based AWS CDK for AWS infrastructure deployment.

The `cdk.json` file tells the CDK Toolkit how to execute your app.

It is a [Maven](https://maven.apache.org/) based project, so you can open this project with any Maven compatible Java IDE to build and run tests.

## Useful commands

- `mvn package` compile and run tests
- `cdk ls` list all stacks in the app
- `cdk synth` emits the synthesized CloudFormation template
- `cdk deploy` deploy this stack to your default AWS account/region
- `cdk diff` compare deployed stack with current state
- `cdk docs` open CDK documentation

Enjoy!

## CDK Clean Up

You can clean up unused CDK assets after successful deployment by running the following command:

```bash
mvn compile exec:java@clean-up
```
