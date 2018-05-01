package twitter.domain.entities

import twitter.domain.entities.enums.ContributorType.ContributorType
import twitter.domain.entities.enums.Hour.Hour
import twitter.domain.entities.enums.Language.Language
import twitter.domain.entities.enums.TimeZone.TimeZone


/**
  * woeid: "Where on Earth Identifiers, by Yahoo!! see http://woeid.rosselliot.co.nz/"
  * */
final case class SettingsOptions(allow_contributor_request: Option[ContributorType] = None,
                                 sleep_time_enabled: Option[Boolean] = None,
                                 start_sleep_time: Option[Hour] = None,
                                 end_sleep_time: Option[Hour] = None,
                                 lang: Option[Language] = None,
                                 time_zone: Option[TimeZone] = None,
                                 trend_location_woeid: Option[Long] = None)
