/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import play.api.libs.json.Json

object CountrySelectOptions {

  lazy val countries = Json
    .parse(locationCanonicalList)
    .as[Seq[Seq[String]]]
    .flatMap {
      case Seq(first, second) =>
        Some(first -> second)
      case _                  =>
        None
    }

  def countryCodeToCountry(code: String): String =
    countries
      .filter {
        case (_, countryCode) =>
          countryCode == s"country:$code" || countryCode == s"territory:$code"
      }
      .map(_._1)
      .headOption
      .getOrElse(throw new IllegalArgumentException(s"No country found for country code $code"))

  private val locationCanonicalList = """[
  [
    "Abu Dhabi",
    "territory:AE-AZ"
  ],
  [
    "Afghanistan",
    "country:AF"
  ],
  [
    "Ajman",
    "territory:AE-AJ"
  ],
  [
    "Akrotiri",
    "territory:XQZ"
  ],
  [
    "Albania",
    "country:AL"
  ],
  [
    "Algeria",
    "country:DZ"
  ],
  [
    "American Samoa",
    "territory:AS"
  ],
  [
    "Andorra",
    "country:AD"
  ],
  [
    "Angola",
    "country:AO"
  ],
  [
    "Anguilla",
    "territory:AI"
  ],
  [
    "Antarctica",
    "territory:AQ"
  ],
  [
    "Antigua and Barbuda",
    "country:AG"
  ],
  [
    "Argentina",
    "country:AR"
  ],
  [
    "Armenia",
    "country:AM"
  ],
  [
    "Aruba",
    "territory:AW"
  ],
  [
    "Ascension",
    "territory:SH-AC"
  ],
  [
    "Australia",
    "country:AU"
  ],
  [
    "Austria",
    "country:AT"
  ],
  [
    "Azerbaijan",
    "country:AZ"
  ],
  [
    "Bahrain",
    "country:BH"
  ],
  [
    "Baker Island",
    "territory:UM-81"
  ],
  [
    "Bangladesh",
    "country:BD"
  ],
  [
    "Barbados",
    "country:BB"
  ],
  [
    "Belarus",
    "country:BY"
  ],
  [
    "Belgium",
    "country:BE"
  ],
  [
    "Belize",
    "country:BZ"
  ],
  [
    "Benin",
    "country:BJ"
  ],
  [
    "Bermuda",
    "territory:BM"
  ],
  [
    "Bhutan",
    "country:BT"
  ],
  [
    "Bolivia",
    "country:BO"
  ],
  [
    "Bonaire",
    "territory:BQ-BO"
  ],
  [
    "Bosnia and Herzegovina",
    "country:BA"
  ],
  [
    "Botswana",
    "country:BW"
  ],
  [
    "Bouvet Island",
    "territory:BV"
  ],
  [
    "Brazil",
    "country:BR"
  ],
  [
    "British Antarctic Territory",
    "territory:BAT"
  ],
  [
    "British Indian Ocean Territory",
    "territory:IO"
  ],
  [
    "British Virgin Islands",
    "territory:VG"
  ],
  [
    "Brunei",
    "country:BN"
  ],
  [
    "Bulgaria",
    "country:BG"
  ],
  [
    "Burkina Faso",
    "country:BF"
  ],
  [
    "Myanmar (Burma)",
    "country:MM"
  ],
  [
    "Burundi",
    "country:BI"
  ],
  [
    "Cambodia",
    "country:KH"
  ],
  [
    "Cameroon",
    "country:CM"
  ],
  [
    "Canada",
    "country:CA"
  ],
  [
    "Cape Verde",
    "country:CV"
  ],
  [
    "Cayman Islands",
    "territory:KY"
  ],
  [
    "Central African Republic",
    "country:CF"
  ],
  [
    "Ceuta",
    "territory:ES-CE"
  ],
  [
    "Chad",
    "country:TD"
  ],
  [
    "Chile",
    "country:CL"
  ],
  [
    "China",
    "country:CN"
  ],
  [
    "Christmas Island",
    "territory:CX"
  ],
  [
    "Cocos (Keeling) Islands",
    "territory:CC"
  ],
  [
    "Colombia",
    "country:CO"
  ],
  [
    "Comoros",
    "country:KM"
  ],
  [
    "Congo",
    "country:CG"
  ],
  [
    "Congo (Democratic Republic)",
    "country:CD"
  ],
  [
    "Cook Islands",
    "territory:CK"
  ],
  [
    "Costa Rica",
    "country:CR"
  ],
  [
    "Croatia",
    "country:HR"
  ],
  [
    "Cuba",
    "country:CU"
  ],
  [
    "Curaçao",
    "territory:CW"
  ],
  [
    "Cyprus",
    "country:CY"
  ],
  [
    "Czechia",
    "country:CZ"
  ],
  [
    "Czechoslovakia",
    "country:CS"
  ],
  [
    "Denmark",
    "country:DK"
  ],
  [
    "Dhekelia",
    "territory:XXD"
  ],
  [
    "Djibouti",
    "country:DJ"
  ],
  [
    "Dominica",
    "country:DM"
  ],
  [
    "Dominican Republic",
    "country:DO"
  ],
  [
    "Dubai",
    "territory:AE-DU"
  ],
  [
    "East Germany",
    "country:DD"
  ],
  [
    "East Timor",
    "country:TL"
  ],
  [
    "Ecuador",
    "country:EC"
  ],
  [
    "Egypt",
    "country:EG"
  ],
  [
    "El Salvador",
    "country:SV"
  ],
  [
    "Equatorial Guinea",
    "country:GQ"
  ],
  [
    "Eritrea",
    "country:ER"
  ],
  [
    "Estonia",
    "country:EE"
  ],
  [
    "Eswatini",
    "country:SZ"
  ],
  [
    "Ethiopia",
    "country:ET"
  ],
  [
    "Falkland Islands",
    "territory:FK"
  ],
  [
    "Faroe Islands",
    "territory:FO"
  ],
  [
    "Fiji",
    "country:FJ"
  ],
  [
    "Finland",
    "country:FI"
  ],
  [
    "France",
    "country:FR"
  ],
  [
    "French Guiana",
    "territory:GF"
  ],
  [
    "French Polynesia",
    "territory:PF"
  ],
  [
    "French Southern Territories",
    "territory:TF"
  ],
  [
    "Fujairah",
    "territory:AE-FU"
  ],
  [
    "Gabon",
    "country:GA"
  ],
  [
    "Georgia",
    "country:GE"
  ],
  [
    "Germany",
    "country:DE"
  ],
  [
    "Ghana",
    "country:GH"
  ],
  [
    "Gibraltar",
    "territory:GI"
  ],
  [
    "Greece",
    "country:GR"
  ],
  [
    "Greenland",
    "territory:GL"
  ],
  [
    "Grenada",
    "country:GD"
  ],
  [
    "Guadeloupe",
    "territory:GP"
  ],
  [
    "Guam",
    "territory:GU"
  ],
  [
    "Guatemala",
    "country:GT"
  ],
  [
    "Guernsey",
    "territory:GG"
  ],
  [
    "Guinea",
    "country:GN"
  ],
  [
    "Guinea-Bissau",
    "country:GW"
  ],
  [
    "Guyana",
    "country:GY"
  ],
  [
    "Haiti",
    "country:HT"
  ],
  [
    "Heard Island and McDonald Islands",
    "territory:HM"
  ],
  [
    "Honduras",
    "country:HN"
  ],
  [
    "Hong Kong",
    "territory:HK"
  ],
  [
    "Howland Island",
    "territory:UM-84"
  ],
  [
    "Hungary",
    "country:HU"
  ],
  [
    "Iceland",
    "country:IS"
  ],
  [
    "India",
    "country:IN"
  ],
  [
    "Indonesia",
    "country:ID"
  ],
  [
    "Iran",
    "country:IR"
  ],
  [
    "Iraq",
    "country:IQ"
  ],
  [
    "Ireland",
    "country:IE"
  ],
  [
    "Isle of Man",
    "territory:IM"
  ],
  [
    "Israel",
    "country:IL"
  ],
  [
    "Italy",
    "country:IT"
  ],
  [
    "Ivory Coast",
    "country:CI"
  ],
  [
    "Jamaica",
    "country:JM"
  ],
  [
    "Japan",
    "country:JP"
  ],
  [
    "Jarvis Island",
    "territory:UM-86"
  ],
  [
    "Jersey",
    "territory:JE"
  ],
  [
    "Johnston Atoll",
    "territory:UM-67"
  ],
  [
    "Jordan",
    "country:JO"
  ],
  [
    "Kazakhstan",
    "country:KZ"
  ],
  [
    "Kenya",
    "country:KE"
  ],
  [
    "Kingman Reef",
    "territory:UM-89"
  ],
  [
    "Kiribati",
    "country:KI"
  ],
  [
    "Kosovo",
    "country:XK"
  ],
  [
    "Kuwait",
    "country:KW"
  ],
  [
    "Kyrgyzstan",
    "country:KG"
  ],
  [
    "Laos",
    "country:LA"
  ],
  [
    "Latvia",
    "country:LV"
  ],
  [
    "Lebanon",
    "country:LB"
  ],
  [
    "Lesotho",
    "country:LS"
  ],
  [
    "Liberia",
    "country:LR"
  ],
  [
    "Libya",
    "country:LY"
  ],
  [
    "Liechtenstein",
    "country:LI"
  ],
  [
    "Lithuania",
    "country:LT"
  ],
  [
    "Luxembourg",
    "country:LU"
  ],
  [
    "Macao",
    "territory:MO"
  ],
  [
    "Madagascar",
    "country:MG"
  ],
  [
    "Malawi",
    "country:MW"
  ],
  [
    "Malaysia",
    "country:MY"
  ],
  [
    "Maldives",
    "country:MV"
  ],
  [
    "Mali",
    "country:ML"
  ],
  [
    "Malta",
    "country:MT"
  ],
  [
    "Marshall Islands",
    "country:MH"
  ],
  [
    "Martinique",
    "territory:MQ"
  ],
  [
    "Mauritania",
    "country:MR"
  ],
  [
    "Mauritius",
    "country:MU"
  ],
  [
    "Mayotte",
    "territory:YT"
  ],
  [
    "Melilla",
    "territory:ES-ML"
  ],
  [
    "Mexico",
    "country:MX"
  ],
  [
    "Micronesia",
    "country:FM"
  ],
  [
    "Midway Islands",
    "territory:UM-71"
  ],
  [
    "Moldova",
    "country:MD"
  ],
  [
    "Monaco",
    "country:MC"
  ],
  [
    "Mongolia",
    "country:MN"
  ],
  [
    "Montenegro",
    "country:ME"
  ],
  [
    "Montserrat",
    "territory:MS"
  ],
  [
    "Morocco",
    "country:MA"
  ],
  [
    "Mozambique",
    "country:MZ"
  ],
  [
    "Namibia",
    "country:NA"
  ],
  [
    "Nauru",
    "country:NR"
  ],
  [
    "Navassa Island",
    "territory:UM-76"
  ],
  [
    "Nepal",
    "country:NP"
  ],
  [
    "Netherlands",
    "country:NL"
  ],
  [
    "New Caledonia",
    "territory:NC"
  ],
  [
    "New Zealand",
    "country:NZ"
  ],
  [
    "Nicaragua",
    "country:NI"
  ],
  [
    "Niger",
    "country:NE"
  ],
  [
    "Nigeria",
    "country:NG"
  ],
  [
    "Niue",
    "territory:NU"
  ],
  [
    "Norfolk Island",
    "territory:NF"
  ],
  [
    "North Korea",
    "country:KP"
  ],
  [
    "North Macedonia",
    "country:MK"
  ],
  [
    "Northern Mariana Islands",
    "territory:MP"
  ],
  [
    "Norway",
    "country:NO"
  ],
  [
    "Occupied Palestinian Territories",
    "territory:PS"
  ],
  [
    "Oman",
    "country:OM"
  ],
  [
    "Pakistan",
    "country:PK"
  ],
  [
    "Palau",
    "country:PW"
  ],
  [
    "Palmyra Atoll",
    "territory:UM-95"
  ],
  [
    "Panama",
    "country:PA"
  ],
  [
    "Papua New Guinea",
    "country:PG"
  ],
  [
    "Paraguay",
    "country:PY"
  ],
  [
    "Peru",
    "country:PE"
  ],
  [
    "Philippines",
    "country:PH"
  ],
  [
    "Pitcairn, Henderson, Ducie and Oeno Islands",
    "territory:PN"
  ],
  [
    "Poland",
    "country:PL"
  ],
  [
    "Portugal",
    "country:PT"
  ],
  [
    "Puerto Rico",
    "territory:PR"
  ],
  [
    "Qatar",
    "country:QA"
  ],
  [
    "Ras al-Khaimah",
    "territory:AE-RK"
  ],
  [
    "Romania",
    "country:RO"
  ],
  [
    "Russia",
    "country:RU"
  ],
  [
    "Rwanda",
    "country:RW"
  ],
  [
    "Réunion",
    "territory:RE"
  ],
  [
    "Saba",
    "territory:BQ-SA"
  ],
  [
    "Saint Barthélemy",
    "territory:BL"
  ],
  [
    "Saint Helena",
    "territory:SH-HL"
  ],
  [
    "Saint Pierre and Miquelon",
    "territory:PM"
  ],
  [
    "Saint-Martin (French part)",
    "territory:MF"
  ],
  [
    "Samoa",
    "country:WS"
  ],
  [
    "San Marino",
    "country:SM"
  ],
  [
    "Sao Tome and Principe",
    "country:ST"
  ],
  [
    "Saudi Arabia",
    "country:SA"
  ],
  [
    "Senegal",
    "country:SN"
  ],
  [
    "Serbia",
    "country:RS"
  ],
  [
    "Seychelles",
    "country:SC"
  ],
  [
    "Sharjah",
    "territory:AE-SH"
  ],
  [
    "Sierra Leone",
    "country:SL"
  ],
  [
    "Singapore",
    "country:SG"
  ],
  [
    "Sint Eustatius",
    "territory:BQ-SE"
  ],
  [
    "Sint Maarten (Dutch part)",
    "territory:SX"
  ],
  [
    "Slovakia",
    "country:SK"
  ],
  [
    "Slovenia",
    "country:SI"
  ],
  [
    "Solomon Islands",
    "country:SB"
  ],
  [
    "Somalia",
    "country:SO"
  ],
  [
    "South Africa",
    "country:ZA"
  ],
  [
    "South Georgia and South Sandwich Islands",
    "territory:GS"
  ],
  [
    "South Korea",
    "country:KR"
  ],
  [
    "South Sudan",
    "country:SS"
  ],
  [
    "Spain",
    "country:ES"
  ],
  [
    "Sri Lanka",
    "country:LK"
  ],
  [
    "St Kitts and Nevis",
    "country:KN"
  ],
  [
    "St Lucia",
    "country:LC"
  ],
  [
    "St Vincent",
    "country:VC"
  ],
  [
    "Sudan",
    "country:SD"
  ],
  [
    "Suriname",
    "country:SR"
  ],
  [
    "Svalbard and Jan Mayen",
    "territory:SJ"
  ],
  [
    "Sweden",
    "country:SE"
  ],
  [
    "Switzerland",
    "country:CH"
  ],
  [
    "Syria",
    "country:SY"
  ],
  [
    "Taiwan",
    "territory:TW"
  ],
  [
    "Tajikistan",
    "country:TJ"
  ],
  [
    "Tanzania",
    "country:TZ"
  ],
  [
    "Thailand",
    "country:TH"
  ],
  [
    "The Bahamas",
    "country:BS"
  ],
  [
    "The Gambia",
    "country:GM"
  ],
  [
    "Togo",
    "country:TG"
  ],
  [
    "Tokelau",
    "territory:TK"
  ],
  [
    "Tonga",
    "country:TO"
  ],
  [
    "Trinidad and Tobago",
    "country:TT"
  ],
  [
    "Tristan da Cunha",
    "territory:SH-TA"
  ],
  [
    "Tunisia",
    "country:TN"
  ],
  [
    "Turkey",
    "country:TR"
  ],
  [
    "Turkmenistan",
    "country:TM"
  ],
  [
    "Turks and Caicos Islands",
    "territory:TC"
  ],
  [
    "Tuvalu",
    "country:TV"
  ],
  [
    "USSR",
    "country:SU"
  ],
  [
    "Uganda",
    "country:UG"
  ],
  [
    "Ukraine",
    "country:UA"
  ],
  [
    "Umm al-Quwain",
    "territory:AE-UQ"
  ],
  [
    "United Arab Emirates",
    "country:AE"
  ],
  [
    "United Kingdom",
    "country:GB"
  ],
  [
    "United States",
    "country:US"
  ],
  [
    "United States Virgin Islands",
    "territory:VI"
  ],
  [
    "Uruguay",
    "country:UY"
  ],
  [
    "Uzbekistan",
    "country:UZ"
  ],
  [
    "Vanuatu",
    "country:VU"
  ],
  [
    "Vatican City",
    "country:VA"
  ],
  [
    "Venezuela",
    "country:VE"
  ],
  [
    "Vietnam",
    "country:VN"
  ],
  [
    "Wake Island",
    "territory:UM-79"
  ],
  [
    "Wallis and Futuna",
    "territory:WF"
  ],
  [
    "Western Sahara",
    "territory:EH"
  ],
  [
    "Yemen",
    "country:YE"
  ],
  [
    "Yugoslavia",
    "country:YU"
  ],
  [
    "Zambia",
    "country:ZM"
  ],
  [
    "Zimbabwe",
    "country:ZW"
  ],
  [
    "Åland Islands",
    "territory:AX"
  ]
]"""
}
