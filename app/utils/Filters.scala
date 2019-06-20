package utils

import javax.inject.Inject

import com.mohiva.play.htmlcompressor.HTMLCompressorFilter
import com.mohiva.play.xmlcompressor.XMLCompressorFilter
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.csrf.CSRFFilter
import play.filters.headers.SecurityHeadersFilter

/**
  * Provides filters.
  */
class Filters @Inject()(csrfFilter: CSRFFilter,
                        securityHeadersFilter: SecurityHeadersFilter,
                        htmlCompressorFilter: HTMLCompressorFilter,
                        xmlCompressorFilter: XMLCompressorFilter) extends HttpFilters {

  override def filters: Seq[EssentialFilter] = Seq(csrfFilter, securityHeadersFilter,
    htmlCompressorFilter, xmlCompressorFilter)

}
