Simple ads.txt crawler. 

Implemented specification: https://iabtechlab.com/~iabtec5/wp-content/uploads/2016/07/IABOpenRTBAds.txtSpecification_Version1_Final.pdf

Application deployed to Heroku: https://ads-txt-crawler.herokuapp.com/

examples: <br/>
https://ads-txt-crawler.herokuapp.com/ads/cnn <br/>
https://ads-txt-crawler.herokuapp.com/ads/wordpress  <br/>
https://ads-txt-crawler.herokuapp.com/ads/nytimes  <br/>

This is the version with Akka parallel approach for getting URL resources and processing responses.

to run app:
`$ sbt clean assembly`
`$ java -jar target/scala-2.13/ads-txt-crawler-assembly-0.1.jar `
or 
`$ sbt run`