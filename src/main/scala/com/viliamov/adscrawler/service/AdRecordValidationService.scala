package com.viliamov.adscrawler.service

import com.viliamov.adscrawler.model.AccountType.AccountType
import com.viliamov.adscrawler.model.{AccountType, AdRecord}
import javax.inject.Singleton
import org.apache.commons.validator.routines.DomainValidator

import scala.collection.mutable

@Singleton
class AdRecordValidationService {
  private final val ERROR_NOT_VALID_CHARS: String = "%s '%s' contains not allowed characters"
  private final val ERROR_NOT_VALID_VALUE: String = "%s '%s' is not valid"
  private final val ERROR_IS_REQUIRED: String = "%s is required"
  private final val ERROR_EMPTY: String = "%s if defined should be not empty"

  private val notRequiredSymbols = Array(",", " ", "\t")
  private val domainValidator = DomainValidator.getInstance()

  private def containSymbols(str: String): Boolean = {
    notRequiredSymbols
      .map(str.contains)
      .exists(el => el)
  }

  def validate(domain: String,
               accountId: String,
               accountType: Option[AccountType],
               authorityId: Option[String] = None
              ): Option[Either[String, AdRecord]] = {
    val errors = new mutable.ArrayBuffer[String]()

    if (domain.isEmpty) {
      errors += ERROR_IS_REQUIRED.format("domain")
    } else if (!domainValidator.isValid(domain)) {
      errors += ERROR_NOT_VALID_VALUE.format("domain", domain)
    }

    if (accountId.isEmpty) {
      errors += ERROR_IS_REQUIRED.format("accountId")
    } else if (containSymbols(accountId)) {
      errors += ERROR_NOT_VALID_CHARS.format("accountId", accountId)
    }

    if (accountType.isEmpty) {
      errors += ERROR_IS_REQUIRED.format("accountType")
    }

    if (authorityId.nonEmpty) {
      if (authorityId.get.isEmpty) {
        errors += ERROR_EMPTY.format("authorityId")
      } else if (containSymbols(authorityId.get)) {
        errors += ERROR_NOT_VALID_CHARS.format("authorityId", authorityId)
      }
    }

    if (errors.nonEmpty) {
      Some(Left(errors.mkString("; ")))
    } else {
      None
    }
  }
}
