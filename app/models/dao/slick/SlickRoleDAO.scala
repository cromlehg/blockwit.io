package models.dao.slick

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import models.Role
import models.dao.RoleDAO
import models.dao.slick.table.RoleTable
import play.api.db.slick.DatabaseConfigProvider
import slick.sql.SqlAction
import models.RoleName

@Singleton
class SlickRoleDAO @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends RoleDAO with RoleTable with SlickCommontDAO {

  import dbConfig.profile.api._
  import scala.concurrent.Future.{ successful => future }

  private val queryByAccountId = Compiled(
    (accountId: Rep[Long]) => table.filter(_.accountId === accountId))

  def _findRolesByAccountId(accountId: Long): SqlAction[Seq[Role], NoStream, Effect.Read] =
    queryByAccountId(accountId).result

  def _addRolesToAccount(userId: Long, rolesIn: RoleName.RoleName*) =
    DBIO.seq(table ++= rolesIn.map(r => models.Role(userId, r)))

  override def findRolesByAccountId(accountId: Long): Future[Seq[Role]] =
    db.run(_findRolesByAccountId(accountId))

  override def close: Future[Unit] =
    future(db.close())

}
