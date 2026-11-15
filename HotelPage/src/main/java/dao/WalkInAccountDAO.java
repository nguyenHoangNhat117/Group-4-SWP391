package dao;

import db.DBContext;
import java.sql.*;
import util.AuthSupport;
import util.AuthUcPolicy;
import util.OfflineCustomerContact;
import util.PasswordUtil;

/** Liên kết tài khoản mới vào đúng Customer đã xác minh email; không tạo Customer mới. */
public class WalkInAccountDAO extends DBContext {
    public Integer eligibleCustomer(String email) throws SQLException {
        String sql="SELECT c.CustomerID,c.FirstName,c.LastName,c.Phone FROM Customer c "
                +"WHERE c.Email=? AND c.UserID IS NULL "
                +"AND NOT EXISTS(SELECT 1 FROM AccountContact a WHERE a.Email=?) "
                +"AND NOT EXISTS(SELECT 1 FROM [User] u WHERE u.Username=?)";
        try(Connection c=getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setNString(1,email);ps.setNString(2,email);ps.setNString(3,email);
            try(ResultSet rs=ps.executeQuery()){
                if(!rs.next())return null;
                int id=rs.getInt(1);
                boolean valid=validName(rs.getString(2)) && validName(rs.getString(3))
                    && (AuthSupport.text(rs.getString(3))+" "+AuthSupport.text(rs.getString(2))).length()<=100
                    && OfflineCustomerContact.validPhone(rs.getString(4));
                if(rs.next() || !valid)return null;
                // Không chọn tùy ý nếu có Customer khác cùng email đã gắn tài khoản.
                try(PreparedStatement all=c.prepareStatement("SELECT COUNT(*) FROM Customer WHERE Email=?")){
                    all.setNString(1,email);try(ResultSet n=all.executeQuery()){n.next();return n.getInt(1)==1?id:null;}
                }
            }
        }
    }

    public int activate(int customerId,String verifiedEmail,String username,String password) throws SQLException {
        if(customerId<=0 || !AuthUcPolicy.validEmail(verifiedEmail)
                || !AuthSupport.validUsername(username) || !AuthSupport.validPassword(password)){
            throw new IllegalArgumentException("Thông tin kích hoạt không hợp lệ.");
        }
        String hash=PasswordUtil.hash(password);
        try(Connection c=getConnection()){
            c.setAutoCommit(false);
            try {
                // Thứ tự kiểm tra giống đăng ký: User -> AccountContact -> Customer.
                String[] checks={
                    "SELECT 1 FROM [User] WITH(UPDLOCK,HOLDLOCK) WHERE Username IN (?,?)",
                    "SELECT 1 FROM AccountContact WITH(UPDLOCK,HOLDLOCK) WHERE Email IN (?,?)",
                    "SELECT 1 FROM Customer WITH(UPDLOCK,HOLDLOCK) WHERE CustomerID<>? AND Email IN (?,?)"
                };
                for(int i=0;i<checks.length;i++){
                    try(PreparedStatement ps=c.prepareStatement(checks[i])){
                        int offset=0;if(i==2){ps.setInt(1,customerId);offset=1;}
                        ps.setNString(1+offset,username);ps.setNString(2+offset,verifiedEmail);
                        try(ResultSet rs=ps.executeQuery()){
                            if(rs.next())throw new IllegalArgumentException("Username/email đã được dùng. Nếu vừa kích hoạt, thử đăng nhập trước khi thực hiện lại.");
                        }
                    }
                }
                String first,last,phone,country,city,street;
                try(PreparedStatement ps=c.prepareStatement("SELECT FirstName,LastName,Phone,Country,City,Street FROM Customer WITH(UPDLOCK,HOLDLOCK) WHERE CustomerID=? AND Email=? AND UserID IS NULL")){
                    ps.setInt(1,customerId);ps.setNString(2,verifiedEmail);
                    try(ResultSet rs=ps.executeQuery()){
                        if(!rs.next())throw new IllegalArgumentException("Hồ sơ đã thay đổi hoặc đã có tài khoản. Kiểm tra đăng nhập hoặc yêu cầu mã mới.");
                        first=AuthSupport.text(rs.getString(1));last=AuthSupport.text(rs.getString(2));phone=OfflineCustomerContact.phone(rs.getString(3));
                        country=rs.getString(4);city=rs.getString(5);street=rs.getString(6);
                    }
                }
                if(!validName(first)||!validName(last)||(last+" "+first).length()>100||!OfflineCustomerContact.validPhone(phone)){
                    throw new IllegalArgumentException("Hồ sơ khách chưa đủ thông tin. Liên hệ lễ tân cập nhật rồi xác minh email lại.");
                }
                int id;
                try(PreparedStatement ps=c.prepareStatement("INSERT [User](Username,[Password],[Role],DisplayName,AccountStatus) VALUES(?,?,'customer',?,'active')",Statement.RETURN_GENERATED_KEYS)){
                    ps.setNString(1,username);ps.setString(2,hash);ps.setNString(3,last+" "+first);
                    if(ps.executeUpdate()!=1)throw new SQLException("User insert failed");
                    try(ResultSet rs=ps.getGeneratedKeys()){if(!rs.next())throw new SQLException("Missing UserID");id=rs.getInt(1);}
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT AccountContact(UserID,FirstName,LastName,Email,Phone,Country,City,Street) VALUES(?,?,?,?,?,?,?,?)")){
                    ps.setInt(1,id);ps.setNString(2,first);ps.setNString(3,last);ps.setNString(4,verifiedEmail);
                    ps.setNString(5,phone);ps.setNString(6,country);ps.setNString(7,city);ps.setNString(8,street);
                    if(ps.executeUpdate()!=1)throw new SQLException("Contact insert failed");
                }
                try(PreparedStatement ps=c.prepareStatement("UPDATE Customer SET UserID=? WHERE CustomerID=? AND UserID IS NULL AND Email=?")){
                    ps.setInt(1,id);ps.setInt(2,customerId);ps.setNString(3,verifiedEmail);
                    if(ps.executeUpdate()!=1)throw new SQLException("Customer link failed");
                }
                c.commit();return id;
            }catch(SQLException|RuntimeException ex){
                try{c.rollback();}catch(SQLException rollback){ex.addSuppressed(rollback);}throw ex;
            }
        }
    }
    private static boolean validName(String value){
        String name=AuthSupport.text(value);
        return !name.isBlank() && name.length()<=50 && name.chars().noneMatch(Character::isISOControl);
    }
}
