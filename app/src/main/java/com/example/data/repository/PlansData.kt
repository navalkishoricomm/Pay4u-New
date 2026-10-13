package com.example.data.repository

import com.example.data.model.RechargePlan

object PlansData {
  val circles = listOf(
    "Pan India",
    "Delhi NCR",
    "Mumbai",
    "Maharashtra & Goa",
    "Karnataka",
    "Tamil Nadu & Chennai",
    "UP East",
    "UP West & Uttarakhand",
    "Gujarat",
    "Rajasthan",
    "Punjab & Haryana",
    "West Bengal & Kolkata",
    "Andhra Pradesh & Telangana",
    "Kerala",
    "Bihar & Jharkhand",
    "Madhya Pradesh & Chhattisgarh"
  )

  fun getPlansForOperator(operator: String): List<RechargePlan> {
    return when (operator.lowercase()) {
      "jio" -> listOf(
        RechargePlan("j1", "Jio", 299.0, "28 Days", "2.0 GB/Day", "Unlimited Calls", "100 SMS/Day", "Popular", "True 5G Unlimited, JioTV, JioCinema, JioCloud"),
        RechargePlan("j2", "Jio", 349.0, "28 Days", "2.5 GB/Day", "Unlimited Calls", "100 SMS/Day", "True 5G Unlimited", "Unlimited 5G Data, High-speed quota with daily reset"),
        RechargePlan("j3", "Jio", 666.0, "70 Days", "1.5 GB/Day", "Unlimited Calls", "100 SMS/Day", "Popular", "Value quarterly pack with unlimited calls and 5G readiness"),
        RechargePlan("j4", "Jio", 899.0, "90 Days", "2.0 GB/Day", "Unlimited Calls", "100 SMS/Day", "Popular", "Hero 90-day pack + 20GB extra bonus data"),
        RechargePlan("j5", "Jio", 3599.0, "365 Days", "2.5 GB/Day", "Unlimited Calls", "100 SMS/Day", "Annual", "365 Days full year validity + True 5G data"),
        RechargePlan("j6", "Jio", 61.0, "Active Plan", "6 GB Total", "No Voice", "None", "Data Booster", "High speed 5G booster add-on on existing plan"),
        RechargePlan("j7", "Jio", 148.0, "28 Days", "10 GB Total", "No Voice", "None", "OTT Addon", "Includes 12 OTT subscriptions including SonyLIV & ZEE5")
      )
      "airtel" -> listOf(
        RechargePlan("a1", "Airtel", 299.0, "28 Days", "1.5 GB/Day", "Unlimited Calls", "100 SMS/Day", "Popular", "Truly Unlimited Calls, Apollo 24|7 Circle, Free Hellotunes"),
        RechargePlan("a2", "Airtel", 379.0, "1 Month", "2.0 GB/Day", "Unlimited Calls", "100 SMS/Day", "True 5G Unlimited", "Calendar Month Validity + Unlimited 5G Data"),
        RechargePlan("a3", "Airtel", 649.0, "56 Days", "2.0 GB/Day", "Unlimited Calls", "100 SMS/Day", "Popular", "Unlimited 5G Data + Airtel Xstream Play 20+ OTTs"),
        RechargePlan("a4", "Airtel", 979.0, "84 Days", "2.0 GB/Day", "Unlimited Calls", "100 SMS/Day", "Popular", "84 days bumper pack + 22+ OTT apps on Xstream"),
        RechargePlan("a5", "Airtel", 3599.0, "365 Days", "2.0 GB/Day", "Unlimited Calls", "100 SMS/Day", "Annual", "Annual 365 Days with Unlimited 5G data"),
        RechargePlan("a6", "Airtel", 65.0, "Active Plan", "4 GB Total", "No Voice", "None", "Data Booster", "Instant data top-up valid till existing pack expiry")
      )
      "vi (vodafone idea)", "vi" -> listOf(
        RechargePlan("v1", "Vi", 299.0, "28 Days", "1.5 GB/Day", "Unlimited Calls", "100 SMS/Day", "Popular", "Binge All Night (12am-6am free unlimited data) + Weekend Data Rollover"),
        RechargePlan("v2", "Vi", 365.0, "28 Days", "2.0 GB/Day", "Unlimited Calls", "100 SMS/Day", "True 5G Unlimited", "Hero Unlimited + 2GB backup data with Data Delight"),
        RechargePlan("v3", "Vi", 719.0, "84 Days", "1.5 GB/Day", "Unlimited Calls", "100 SMS/Day", "Popular", "Quarterly Pack + Binge All Night + Vi Movies & TV"),
        RechargePlan("v4", "Vi", 3199.0, "365 Days", "2.0 GB/Day", "Unlimited Calls", "100 SMS/Day", "Annual", "Annual pack with 1 Year Disney+ Hotstar mobile subscription included"),
        RechargePlan("v5", "Vi", 49.0, "1 Day", "20 GB Total", "No Voice", "None", "Data Booster", "Super hourly/night unlimited data pass")
      )
      "bsnl" -> listOf(
        RechargePlan("b1", "BSNL", 107.0, "35 Days", "3 GB Total", "200 Mins Voice", "None", "Popular", "Budget long validity saver pack with 200 free calling minutes"),
        RechargePlan("b2", "BSNL", 197.0, "70 Days", "2.0 GB/Day (15 days)", "Unlimited (15 days)", "100 SMS/Day (15 days)", "Popular", "70 Days validity extension + 15 days unlimited calls"),
        RechargePlan("b3", "BSNL", 397.0, "150 Days", "2.0 GB/Day (30 days)", "Unlimited (30 days)", "100 SMS/Day (30 days)", "Popular", "150 Days incoming validity guarantee pack"),
        RechargePlan("b4", "BSNL", 797.0, "300 Days", "2.0 GB/Day (60 days)", "Unlimited (60 days)", "100 SMS/Day", "Annual", "300 Days extended validity pack for secondary SIM"),
        RechargePlan("b5", "BSNL", 98.0, "22 Days", "2.0 GB/Day", "No Voice", "None", "Data Booster", "High speed data voucher for 22 days")
      )
      "tata play (tata sky)", "tata play", "tatasky" -> listOf(
        RechargePlan("t1", "Tata Play", 249.0, "1 Month", "85+ SD Channels", "Hindi Entertainment", "N/A", "Popular", "Hindi Smart Plan with Star Plus, Zee TV, Sony, Colors"),
        RechargePlan("t2", "Tata Play", 399.0, "1 Month", "110+ HD/SD Channels", "All Entertainment + Movies HD", "N/A", "Popular", "Hindi Dhamaal HD pack including top movies & music channels"),
        RechargePlan("t3", "Tata Play", 499.0, "1 Month", "145+ HD Channels", "Sports + HD Movies Mega", "N/A", "Popular", "Star Sports 1 HD, Sony Sports, Cricket + Cinema"),
        RechargePlan("t4", "Tata Play", 2499.0, "6 Months", "150+ Channels", "Semi-Annual Saver", "N/A", "Annual", "6-Month uninterrupted viewing with discount bonus"),
        RechargePlan("t5", "Tata Play", 4899.0, "12 Months", "All HD Channels", "Annual Complete HD", "N/A", "Annual", "365 Days complete family entertainment & all sports packs")
      )
      "airtel digital tv", "airteldth" -> listOf(
        RechargePlan("ad1", "Airtel Digital TV", 260.0, "1 Month", "90+ Channels", "Dabang SD Combo", "N/A", "Popular", "Complete Hindi entertainment and news bouquet"),
        RechargePlan("ad2", "Airtel Digital TV", 420.0, "1 Month", "120+ Channels", "All Entertainment HD", "N/A", "Popular", "Full HD movies, regional feeds, sports channels"),
        RechargePlan("ad3", "Airtel Digital TV", 2399.0, "6 Months", "130+ Channels", "6 Months Super Saver", "N/A", "Annual", "Half-yearly recharge with free service visit coupons")
      )
      "dish tv", "dishtv" -> listOf(
        RechargePlan("dt1", "Dish TV", 225.0, "1 Month", "80+ Channels", "Classic Hindi SD", "N/A", "Popular", "Essential family television pack"),
        RechargePlan("dt2", "Dish TV", 360.0, "1 Month", "105+ HD Channels", "Family HD Value Combo", "N/A", "Popular", "Popular HD channels + Cricket channels"),
        RechargePlan("dt3", "Dish TV", 1999.0, "12 Months", "100+ Channels", "Annual Platinum Super", "N/A", "Annual", "Best value annual subscription pack")
      )
      else -> listOf(
        RechargePlan("gen1", operator, 299.0, "28 Days", "1.5 GB/Day", "Unlimited Calls", "100 SMS/Day", "Popular", "Standard Monthly Unlimited pack"),
        RechargePlan("gen2", operator, 499.0, "56 Days", "1.5 GB/Day", "Unlimited Calls", "100 SMS/Day", "Popular", "2-Month validity saver pack"),
        RechargePlan("gen3", operator, 799.0, "84 Days", "2.0 GB/Day", "Unlimited Calls", "100 SMS/Day", "Popular", "Quarterly pack with max benefits")
      )
    }
  }
}
