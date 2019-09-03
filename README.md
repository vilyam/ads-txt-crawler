Simple ads.txt crawler using Scala and Akka. 

Implemented specification: https://iabtechlab.com/~iabtec5/wp-content/uploads/2016/07/IABOpenRTBAds.txtSpecification_Version1_Final.pdf

examples: <br/>
https://ads-txt-crawler.herokuapp.com/ads/cnn <br/>
https://ads-txt-crawler.herokuapp.com/ads/wordpress  <br/>
https://ads-txt-crawler.herokuapp.com/ads/nytimes  <br/>

This is the version with Akka parallel approach for getting URL resources and processing responses.

to run app:<br/>
`$ sbt clean assembly`<br/>
`$ java -jar target/scala-2.13/ads-txt-crawler-assembly-0.1.jar `<br/>
or <br/>
`$ sbt run`
