package com.napkinstudio.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.napkinstudio.entity.User;
import com.napkinstudio.sapcommunicationmodels.DataTransferFromSAP;
import com.napkinstudio.sapcommunicationmodels.DataTransferToSAP;
//import com.napkinstudio.sapcommunicationmodels.DataTransferFromSAP;
//import com.napkinstudio.sapcommunicationmodels.DataTransferToSAP;
import com.thoughtworks.xstream.XStream;


@Service("ftpService")
public class FTPManager {
	
	@Autowired
	XStream xstream;
	
//	@Autowired
	FTPSClient ftpClient;
	
	public String handle() {
		String message = "ok";
		
		String 	host = "localhost",//"10.4.0.129",
				username = "catdogcat",
				password = "2cats1dog";
		
		int 	port = 21;
		
		String 	pathToIsBusyFile = "checkisbusy.txt",
				pathToKeepInSyncFile = "keepinsync.txt",
				pathToFileToSAP = "DataTransferToSAP.xml",
				pathToFileFromSAP = "DataTransferFromSAP.xml";
		
        InputStream
        		is_ = null,
        		is0 = null,
        		is1 = null;
        OutputStream
        		os_ = null,
        		os0 = null,
        		os1 = null,
        		os2 = null;
        String str;
        String[] strArr;
        String 	fileFromSAPStatus = "delivered",
        		fileToSAPStatus = "accepted";
        boolean	isBusyChanged = false;
        byte[] bytes;
        DataTransferFromSAP dtfs;
        DataTransferToSAP dtts;
        
        BufferedReader reader = null;
        StringBuffer buf = new StringBuffer(1000);
        
        
		try {
			// Connect to host
			ftpClient = new FTPSClient(false);
			ftpClient.connect(host, port);
			int reply = ftpClient.getReplyCode();
			if (FTPReply.isPositiveCompletion(reply)) {

				// Login
				if (ftpClient.login(username, password)) {
					// Set protection buffer size
					ftpClient.execPBSZ(0);
					// Set data channel protection to private
					ftpClient.execPROT("P");
					// Enter local passive mode
					ftpClient.enterLocalPassiveMode();
					
//					ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
					
					
					
					is_ = ftpClient.retrieveFileStream(pathToIsBusyFile);
					if(is_ != null) {
						reader = new BufferedReader(new InputStreamReader(is_));
						if((str = reader.readLine()) != null) {
							if(str.equals("busy")) {
								System.out.println("Wait your turn!");
								return "Wait your turn!";
							} else {
								System.out.println("It's your turn!");
								isBusyChanged = true;
								reader.close();
								is_.close();
								System.out.println("is_ is closed? Answer: " + ftpClient.completePendingCommand());
								os_ = ftpClient.storeFileStream(pathToIsBusyFile);
								if(os_ != null) {
									os_.write("busy".getBytes());
									os_.flush();
									os_.close();
									System.out.println("2)os_ is closed? Answer: " + ftpClient.completePendingCommand());
								} else {
									System.out.println("Can't open outputstream <os_> to checkisbusy.txt!");
				            		System.out.println(ftpClient.getReplyString());
				            		return "2) Can't connect to checkisbusy.txt!";
								}
							}
						} else {
							reader.close();
							System.out.println("Wait your turn!");
							return "Wait your turn!";
						}
					} else {
						System.out.println("Can't open inputstream <is_> to checkisbusy.txt!");
	            		System.out.println(ftpClient.getReplyString());
	            		return "1) Can't connect to checkisbusy.txt!";
					}
					
					
					
					is0 = ftpClient.retrieveFileStream(pathToKeepInSyncFile);	
					if (is0 != null) {
						reader = new BufferedReader(new InputStreamReader(is0));
		                while ((str = reader.readLine()) != null) {
		                	strArr = str.split(":");
		                	if(strArr.length >= 2) {
		                		if(strArr[0].trim().equals("fileFromSAPStatus")) {
		                			if(strArr[1].trim().equals("accepted")) {
		                				fileFromSAPStatus = "accepted";
		                			} else if(strArr[1].trim().equals("delivered")) {
		                				fileFromSAPStatus = "delivered";
		                			}
		                			if(strArr[1].trim().equals("accepted")) {
		                				fileFromSAPStatus = "accepted";
		                			} else if(strArr[1].trim().equals("delivered")) {
		                				fileFromSAPStatus = "delivered";
		                			}
		                		}
		                		if(strArr[0].trim().equals("fileToSAPStatus")) {
		                			if(strArr[1].trim().equals("accepted")) {
		                				fileToSAPStatus = "accepted";
		                			} else if(strArr[1].trim().equals("delivered")) {
		                				fileToSAPStatus = "delivered";
		                			}
		                			if(strArr[1].trim().equals("accepted")) {
		                				fileToSAPStatus = "accepted";
		                			} else if(strArr[1].trim().equals("delivered")) {
		                				fileToSAPStatus = "delivered";
		                			}
		                		}
		                	}
		                	System.out.println(str);
		                    buf.append(str + "\n" );
		                }
		                reader.close();
		                is0.close();
		                System.out.println("is0 is closed? Answer: " + ftpClient.completePendingCommand());
		            } else {
		            	System.out.println("Can't open inputstream <is0> to keepinsyck.txt!");
	            		System.out.println(ftpClient.getReplyString());
	            		return "Can't connect to keepinsync.txt!";
		            }
					
					
		            if(fileFromSAPStatus.equals("accepted")) {
		            	System.out.println("File from SAP has been already accepted!");
		            } else if(fileFromSAPStatus.equals("delivered")) {
		            	//SAP file was delivered by SAP, so Portal must accept it
		            	is1 = ftpClient.retrieveFileStream(pathToFileFromSAP);
		            	if(is1 != null) {
		            		dtfs = (DataTransferFromSAP) xstream.fromXML(is1);
			                //write to db
			                System.out.println(dtfs);
			                
			                fileFromSAPStatus = "accepted";
			                is1.close();
			                System.out.println("is1 is closed? Answer: " + ftpClient.completePendingCommand());
		            	}
		            	
		            }
		            if(fileToSAPStatus.equals("accepted")) {
		            	//Portal file was accepted by SAP, so new portal file can be uploaded
		            	os2 = ftpClient.storeFileStream(pathToFileToSAP);
		            	if(os2 != null) {
		            		dtts = new DataTransferToSAP();
			                User us = new User();
			                us.setEnabled(true);
			                us.setFirstName("Cheryl");
			                us.setLastName("Brooks");
			                us.setLastModifiedDate(new Date());
			                us.setLogin("trdd");
			                dtts.setUser(us);
			                //read from db
			                xstream.toXML(dtts, os2);
			                
			                fileToSAPStatus = "delivered";
			                os2.flush();
			                os2.close();
			                System.out.println("os2 is closed? Answer: " + ftpClient.completePendingCommand());
		            	} else {
		            		System.out.println("Can't open outputstream <os2> to FileToSAP!");
		            		System.out.println(ftpClient.getReplyString());
		            	}
		            } else if(fileToSAPStatus.equals("delivered")) {
		            	//Portal file has not been accepted by SAP yet, so new portal file must not be uploaded
		            	System.out.println("Portal file has not been accepted by SAP yet, so new portal file must not be uploaded!");
		            }
		            
		            
		            bytes = ("fileFromSAPStatus:\t" + fileFromSAPStatus + "\r\n"
		            		+ "fileToSAPStatus:\t" + fileToSAPStatus + "\r\n").getBytes();
		            os0 = ftpClient.storeFileStream("keepinsync.txt");
		            if(os0 != null) {
		            	os0.write(bytes);
		            	os0.flush();
		            	os0.close();
		            	System.out.println("os0 is closed? Answer: " + ftpClient.completePendingCommand());
		            } else {
		            	System.out.println("Can't open outputstream <os0> to FileToSAP!");
	            		System.out.println(ftpClient.getReplyString());
		            }
		            
		            //change status in checkisbusy.txt file from "busy" to "opened"
		            if(isBusyChanged) {
		            	os_.close();
		            	os_ = ftpClient.storeFileStream(pathToIsBusyFile);
	        			if(os_ != null) {
	        				os_.write("opened".getBytes());
	        				os_.flush();
	        				os_.close();
	        				System.out.println("os_ is closed? Answer: " + ftpClient.completePendingCommand());
	        				isBusyChanged = false;
	        			} else {
							System.out.println("checkisbusy.txt must be opened again!");
						}
	        		}
					
					// Logout
					ftpClient.logout();

				} else {
					System.out.println("FTP login failed");
				}

				// Disconnect
				ftpClient.disconnect();
				System.out.println("operations over!");
			} else {
				System.out.println("FTP connect to host failed");
			}
		} catch (IOException ioe) {
			System.out.println("FTP client received network error");
			message = "FTP client received network error";
			ioe.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(reader != null) reader.close();
				
				if(is_ != null) is_.close();
        		if(is0 != null) is0.close();
        		if(is1 != null) is1.close();
        		
        		if(isBusyChanged) {
        			if(os_ == null) {
        				is_ = ftpClient.retrieveFileStream(pathToIsBusyFile);
        			}
        			if(os_ != null) {
        				os_.write("opened".getBytes());
        				os_.flush();
        				os_.close();
        				System.out.println("os_ is closed? Answer: " + ftpClient.completePendingCommand());
        			} else {
						System.out.println("checkisbusy.txt must be opened again!");
					}
        		}
        		if(os0 != null) os0.close();
        		if(os1 != null) os1.close();
        		if(os2 != null) os2.close();
        	} catch (IOException ex) {
                ex.printStackTrace();
            } finally {
            	if(os_ != null)
					try {
						os_.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            }
			// Disconnect
			if(ftpClient != null) {
				try {
					// Logout
//					ftpClient.logout();
					ftpClient.disconnect();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return message;
	}
	
//	private static String getStringFromInputStream(InputStream is) {
//
//		BufferedReader br = null;
//		StringBuilder sb = new StringBuilder();
//
//		String line;
//		try {
//
//			br = new BufferedReader(new InputStreamReader(is));
//			while ((line = br.readLine()) != null) {
//				sb.append(line);
//			}
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (br != null) {
//				try {
//					br.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//		return sb.toString();
//
//	}
	
}
