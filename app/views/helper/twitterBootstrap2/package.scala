package views.html.helper.twitterBootstrap2

import views.html.helper._

/**
 * Contains template helpers, for example for generating HTML forms.
 */
object Bootstrap2Helpers {

  /**
   * Twitter bootstrap input structure.
   *
   * {{{
   * <dl>
   *   <dt><label for="username"></dt>
   *   <dd><input type="text" name="username" id="username"></dd>
   *   <dd class="error">This field is required!</dd>
   *   <dd class="info">Required field.</dd>
   * </dl>
   * }}}
   */
  implicit val twitterBootstrap2Field = new FieldConstructor {
    def apply(elts: FieldElements) = twitterBootstrap2FieldConstructor(elts)
  }

}
