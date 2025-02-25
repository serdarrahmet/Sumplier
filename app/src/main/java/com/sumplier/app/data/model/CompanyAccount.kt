package com.sumplier.app.data.model

data class CompanyAccount(

    val id: Long,
    val accountName: String,
    val address: String?,
    val region: String?,
    val city: String?,
    val isActive: Boolean?,
    val licenceCode: String?,
    val companyCode: Long?

)
