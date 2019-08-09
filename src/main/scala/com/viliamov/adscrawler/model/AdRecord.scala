package com.viliamov.adscrawler.model

import com.viliamov.adscrawler.model.AccountType.AccountType

case class AdRecord(domain: String, accountId: String, accountType: AccountType, authorityId: Option[String] = None)

