
package com.zeomega.services.pip;

import org.wso2.carbon.identity.entitlement.pip.AbstractPIPAttributeFinder;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


public class JivaUserRoleAttributeFinder extends AbstractPIPAttributeFinder {

    
    private static final String GROUP_ID = "http://zeomega.com/id/role";

    private static final String POINT_ID = "http://zeomega.com/id/age";

    private static final String EMAIL_ID = "http://zeomega.com/id/email";

	/**
	 * Connection pool is used to create connection to database
	 */
	private DataSource dataSource;

	/**
	 * List of attribute finders supported by the this PIP attribute finder
	 */
	private Set<String> supportedAttributes = new HashSet<String>();

    @Override
	public void init(Properties properties)  throws Exception{
        
        String dataSourceName = (String) properties.get("DataSourceName");

        if(dataSourceName == null || dataSourceName.trim().length() == 0){
            throw new Exception("Data source name can not be null. Please configure it in the entitlement.properties file.");
        }

        dataSource = (DataSource) InitialContext.doLookup(dataSourceName);

        supportedAttributes.add(GROUP_ID);
        supportedAttributes.add(POINT_ID);
        supportedAttributes.add(EMAIL_ID);
    }

    @Override
    public String getModuleName() {
        return "Jiva-User-Role Attribute Finder";
    }

    @Override
    public Set<String> getAttributeValues(String subjectId, String resourceId, String actionId,
                                          String environmentId, String attributeId, String issuer) throws Exception{

		String attributeName = null;

        if(GROUP_ID.equals(attributeId)){
            attributeName = "role";
        } else if(POINT_ID.equals(attributeId)){
            attributeName = "age";
        } else if(EMAIL_ID.equals(attributeId)){
            attributeName = "email";
        }

        if(attributeName == null){
            throw new Exception("Invalid attribute id : " + attributeId);
        }

        /**
		 * SQL statement to retrieve attribute value for given attribute id from database
		 */
		String sqlStmt = "select user_role.user_type_id from user_role, sys_user where sys_user.user_idn = user_role.user_idn and sys_user.sys_user_id='"
				+ subjectId +"';";

		Set<String> values = new HashSet<String>();
		PreparedStatement prepStmt = null;
		ResultSet resultSet = null;
		Connection connection = null;

		try {
			connection = dataSource.getConnection();
			if (connection != null) {
				prepStmt = connection.prepareStatement(sqlStmt);
				resultSet = prepStmt.executeQuery();
				while (resultSet.next()) {
					values.add(resultSet.getString(1));
				}
			}
		} catch (SQLException e) {
			throw new Exception("Error while retrieving attribute values", e);
		}finally {
            try{
                if(resultSet != null){
                    resultSet.close();
                }
                if(prepStmt != null){
                    prepStmt.close();
                }
                if(connection !=  null){
                    connection.close();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }

		return values;
	}
    
    @Override
	public Set<String> getSupportedAttributes() {
		return supportedAttributes;
	}
}
