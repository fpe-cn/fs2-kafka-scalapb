/*
 * Copyright 2021-2021 Financiere des Paiements Electroniques
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package fs2.kafka.scalapb

/**
  * The available options for [[SchemaRegistryClientSettings#withAuth]].
  *
  * Available options include:<br>
  * - [[Auth.Basic]] to authenticate with username and password,<br>
  * - [[Auth.Bearer]] to authenticate with an authentication token,<br>
  * - [[Auth.None]] to not provide any authentication details.
  */
sealed abstract class Auth

object Auth {
  private[scalapb] final case class BasicAuth(username: String, password: String) extends Auth {
    override def toString: String = s"Basic($username)"
  }

  private[scalapb] final case class BearerAuth(token: String) extends Auth {
    override def toString: String = "Bearer"
  }

  private[scalapb] case object NoAuth extends Auth {
    override def toString: String = "None"
  }

  /**
    * Option to authenticate with username and password.
    */
  def Basic(username: String, password: String): Auth =
    BasicAuth(username, password)

  /**
    * Option to authenticate with an authentication token.
    */
  def Bearer(token: String): Auth =
    BearerAuth(token)

  /**
    * Option to not provide any authentication details.
    */
  val None: Auth =
    NoAuth
}
