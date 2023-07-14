
import com.google.inject.Inject
import models.requests.DataRequest
import models.{DraftId, Mode, TraderDetailsWithCountryCode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat
import views.html.TraderCheckRegisteredDetailsView

package userrole {
  case class AgentForTrader @Inject()(view: TraderCheckRegisteredDetailsView) extends UserRole {
  override def selectViewForCheckRegDetails(
     form: Form[Boolean],
     details: TraderDetailsWithCountryCode,
     mode: Mode,
     draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
    view(
      form,
      details,
      mode,
      draftId
    )

  }
}

