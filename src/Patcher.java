import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JTextArea;

public class Patcher {

	public static void patch(File file, JTextArea log) {

		if (file == null || file.canRead() == false) {
			log.append("Can not read!\n");
			return;
		}

		long now = System.currentTimeMillis();

		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"
					+ file.getAbsolutePath());
			conn.setAutoCommit(false);
			Statement stat = conn.createStatement();
			ResultSet rs = null;
			try {
				rs = stat
						.executeQuery("select _id, data1 from data where data1 like '8%' or data1 like '+995%' or data1 like '00995%'");
			} catch (Exception ex) {
				ex.printStackTrace();
				log.append("Can not query the database! Right file? from /data/data/com.android.providers.contacts/databases/\n");
				return;
			}
			PreparedStatement ps = conn
					.prepareStatement("update data set data1 = ? where _id = ?");
			while (rs.next()) {
				String id = rs.getString(1);
				String number = rs.getString(2);

				log.append("Patching number " + number);
				// log.append("\n");
				String new_number = null;
				if (number.startsWith("8")) {
					if (number.startsWith("822") || number.startsWith("832")) {// landline
						new_number = number.substring(0, 3) + "2"
								+ number.substring(3);
					} else if (number.startsWith("87")
							|| number.startsWith("85")
							|| number.startsWith("89")) { //
						new_number = "5" + number.substring(1);
					} else {
						log.append(" Unknown nummer?!, skipping...\n");
						continue;
					}
				} else if (number.startsWith("+995")) {
					if (number.startsWith("+99532")) {
						new_number = "+995322" + number.substring(6);
					} else if (number.startsWith("+9955")
							|| number.startsWith("+9959")
							|| number.startsWith("+9957")) {
						new_number = "+9955" + number.substring(4);
					} else {
						log.append("Can not patch, skipping....\n");
						continue;
					}
				} else if (number.startsWith("0")) {
					if (number.startsWith("0099532")) {
						new_number = "00995322" + number.substring(7);
					} else if (number.startsWith("009955")
							|| number.startsWith("009959")
							|| number.startsWith("009957")) {
						new_number = "009955" + number.substring(5);
					} else {
						log.append("Can not patch, skipping....\n");
						continue;
					}
				} else {
					log.append("Can not patch, skipping....\n");
					continue;
				}
				//if (true) {
					//log.append(" ->  " + new_number + " \n");
					//continue;
				//}
				ps.setString(1, new_number);
				ps.setString(2, id);
				ps.execute();
				log.append(" ->  " + new_number + " \n");
				//} else {
					//log.append("Can not patch, skipping....\n");
				//}
			}

			conn.commit();
			rs.close();
			conn.close();
			log.append("done in " + (System.currentTimeMillis() - now) + "ms");

		} catch (Exception e) {
			e.printStackTrace();
			log.append(e.getMessage() + "\n");
		}

	}
}
