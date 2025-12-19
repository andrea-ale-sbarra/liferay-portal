## Steps to deploy  liferay-commerceaichatbot

### Execute the following commands

1. /gradlew [liferay-aicontentwizard-batch](../liferay-aicontentwizard-workspace/client-extensions/liferay-aicontentwizard-batch)  
2. npm install [liferay-aicontentwizard-custom-element](../liferay-aicontentwizard-workspace/client-extensions/liferay-aicontentwizard-custom-element)
3. ./gradlew [liferay-aicontentwizard-custom-element](../liferay-aicontentwizard-workspace/client-extensions/liferay-aicontentwizard-custom-element)
4. ./gradlew [liferay-aicontentwizard-etc-spring-boot](../liferay-aicontentwizard-workspace/client-extensions/liferay-aicontentwizard-etc-spring-boot)

### Enable the following feature flags

1. LPD-48862

## Start apps  

1. liferay-aicontentwizard-etc-spring-boot

a. export LIFERAY_PASSWORD=<the 66degrees test portal password>
b. ./gradlew bootRun

2. liferay-aicontentwizard-custom-element

npm run dev