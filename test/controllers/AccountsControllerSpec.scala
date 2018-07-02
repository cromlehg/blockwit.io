package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import play.api.db.Database

class AccountsControllerSpec() extends PlaySpec with GuiceOneAppPerTest with Injecting with BaseTestApplicationFactory {

  "AccountsController" should {

    "render the login page" in {
      val result = route(app, FakeRequest(GET, "/app/login")).get
      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("login")
      contentAsString(result) must include("pass")
    }

    "wrong login or password" in {
      val result = route(app, FakeRequest(POST, "/app/login")
        .withFormUrlEncodedBody(
          "email" -> "wronglogin@wrongproject.wronjcoutry",
          "pass" -> "badPassword123456789")).get

      status(result) mustBe BAD_REQUEST

      // FIXME: Why not works?
      //flash(result).get("error") mustBe Some(message("app.login.error"))
      contentType(result) mustBe Some("text/html")

      contentAsString(result) must include("login")
      contentAsString(result) must include("pass")
    }

    "login success" in {
      val result = route(app, FakeRequest(POST, "/app/login")
        .withFormUrlEncodedBody(
          "email" -> "testadmin@project.country",
          "pass" -> "$2a$10$7DoFfvhSb5ZZytPUBLip.en3GW6Vc2BoIps02WgsfNeIhEZSS7KIS")).get

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some("/")

      val redirectResult = route(
        app,
        FakeRequest(GET, redirectLocation(result).get)
          .withSession(session(result).data.toMap.toSeq: _*)
          .withCookies(cookies(result).toSeq: _*)).get

      status(redirectResult) mustBe OK
      contentType(redirectResult) mustBe Some("text/html")
      contentAsString(redirectResult) must include("logout")
    }

    //      redirectLocation(result) mustBe Some("/app/login")

    //    "process login" in {
    //      val result = route(app, FakeRequest(POST, "/app/login")
    //        .withFormUrlEncodedBody(
    //          "email" -> "testadmin@project.country",
    //          "pass" -> "$2a$10$7DoFfvhSb5ZZytPUBLip.en3GW6Vc2BoIps02WgsfNeIhEZSS7KIS")).get
    //
    //      status(result) mustBe SEE_OTHER
    //
    //      redirectLocation(result) must "/index"
    //
    //      redirectLocation(result) must beSome("/index")
    //
    //      flash(result).get("message") must beSome("You are not logged in.")
    //
    //      contentType(result) mustBe Some("text/html")
    //      contentAsString(result) must include("login")
    //      contentAsString(result) must include("pass")
    //    }

  }

}
