package com.viliamov.adscrawler.model

object AccountType extends Enumeration {
  type AccountType = Value
  val DIRECT = Value
  val RESELLER = Value

  def withNameOpt(s: String): Option[AccountType] = values.find(_.toString.equalsIgnoreCase(s))
}
