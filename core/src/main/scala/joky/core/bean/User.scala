package joky.core.bean

import java.sql.Date

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/11 21:31
  * @Description:
  */
case class User(id: String,
                company_id: String,
                email: String,
                mobile: String,
                name: String,
                position: String,
                password: String,
                status: String,
                create_time: Date,
                update_time: Date) {

}
