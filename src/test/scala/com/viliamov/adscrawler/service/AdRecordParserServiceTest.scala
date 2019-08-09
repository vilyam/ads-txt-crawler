package com.viliamov.adscrawler.service

import com.google.inject.{Guice, Injector}
import com.viliamov.adscrawler.CommonModule
import com.viliamov.adscrawler.dao.DaoModule
import org.scalatest.{FunSuite, GivenWhenThen, PrivateMethodTester}

class AdRecordParserServiceTest extends FunSuite with GivenWhenThen with PrivateMethodTester {
  val injector: Injector = Guice.createInjector(new CommonModule(), new ParserModule(), new DaoModule)

  val service: AdRecordParserService = injector.getInstance(classOf[AdRecordParserService])

  test("prepare") {
    Given("the raw ads list")
    val raw =
      """# CNN.com/ads.txt
         # FAKE LINE
         # DOMESTIC
         xdfhsdfh
         !google.com,        pub-74392813 11086140,
         google.com,          pub-743928	1311086140,DIRECT,
         google.com,          pub-7439281311086140,DIRECT,         f08c47fec0942fa0 # comment
         rubiconproject.com,11078,         DIRECT,          0bfd66d529a55807 # banner, video
         c.amazon-adsystem.com,     3159,     DIRECT     # banner, video"""

    When("the Set of records is prepared")
    val seq = service.prepare(raw)

    val actual = seq.mkString("\n")

    Then("the Set should be non empty")
    assert(seq.nonEmpty)

    And("the Set should not contains comments")
    assert(!actual.contains("#"))
    assert(!actual.contains("comment"))
    assert(!actual.contains("banner, video"))
    assert(!actual.contains("FAKE LINE"))
  }

  test("parseLine") {
    Given("the Set of lines")
    val seq = List(
      "google.com,          pub-7439281311086140, DIRECT,         f08c47fec0942fa0",
      "!google.com,        pub-74392813 11086140,",
      "google.com,          pub-743928	1311086140,DIRECT,",
      "xdfhsdfh")

    When("the Set of lines is parsed")
    val parsed = seq.map(service.parseLine)

    Then("the Set should contains Right Value")
    assert(parsed.exists(_.isRight))

    And("should catch domain validation error")
    assert(parsed.filter(_.isLeft).exists(_.toString.contains("domain '!google.com' is not valid")))

    And("should catch not allowed characters error")
    assert(parsed.filter(_.isLeft).exists(_.toString.contains("accountId 'pub-74392813 11086140' contains not allowed characters")))
    assert(parsed.filter(_.isLeft).exists(_.toString.contains("accountId 'pub-743928	1311086140' contains not allowed characters")))

    And("should catch required fields error")
    assert(parsed.filter(_.isLeft).exists(_.toString.contains("accountType is required")))

    And("should catch formatting error")
    assert(parsed.filter(_.isLeft).exists(_.toString.contains("authorityId if defined should be not empty")))
    assert(parsed.filter(_.isLeft).exists(_.toString.contains("Cannot parse 'xdfhsdfh'")))
  }
}
