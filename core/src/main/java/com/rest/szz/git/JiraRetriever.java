package com.rest.szz.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JiraRetriever {
	private String jiraURL;
	private String projectName;
	private URL url;
	private URLConnection connection;
	private Document d;
	private PrintWriter pw;
	private String isBrokenByLinkName;


	/**
	 * Class for retrieving all Jira issues. The retrieval must be done only if
	 * the csv not yet present it. Otherwise it must be just updated.
	 *
	 * @param jiraURL
	 * @param projectName
	 */
	public JiraRetriever(String jiraURL, String projectName, String isBrokenByLinkName) {
		this.jiraURL = jiraURL;
		this.projectName = projectName;
		this.isBrokenByLinkName = isBrokenByLinkName;
		try {
			 pw = new PrintWriter(new FileOutputStream(
				    new File("home" + File.separator + projectName + "-log.txt"),
				    true /* append = true */));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * It gets a the XML Document from the stream
	 *
	 * @param stream
	 * @return
	 */
	private Document parseXML(InputStream stream) {
		DocumentBuilderFactory objDocumentBuilderFactory = null;
		DocumentBuilder objDocumentBuilder = null;
		Document doc = null;
		try {
			objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
			objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();
			doc = objDocumentBuilder.parse(stream);
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		return doc;
	}

	private int getTotalNumberIssues(){
		String tempQuery = "?jqlQuery=project+%3D+{0}+ORDER+BY+key+DESC&issuetype=Bug&tempMax=1";
		tempQuery = tempQuery.replace("{0}", projectName);
		try {
			url = new URL(jiraURL + tempQuery);
			connection = url.openConnection();
			d = parseXML(connection.getInputStream());
			NodeList descNodes = d.getElementsByTagName("item");
			Node node = descNodes.item(0);
			for (int p = 0; p < node.getChildNodes().getLength(); p++) {
			if (node.getChildNodes().item(p).getNodeName().equals("key")){
				String key = (node.getChildNodes().item(p).getTextContent());
				key = key.replaceFirst(".*?(\\d+).*", "$1");
				return Integer.parseInt(key);
			}}
		} catch (Exception e) {
			pw.println(e.getMessage());
		}
		return 0;
	}


	public void printIssues(){
		int page = 0;
		int totalePages = (int) Math.ceil(((double) getTotalNumberIssues() / 1000));
		String fileName = projectName + "_" + page + ".csv";
		File file = new File("home" + File.separator + projectName + "/" + fileName);
		while (file.exists() ) {
			page++;
			fileName = projectName + "_" + page + ".csv";
			file = new File("home" + File.separator + projectName + "/" + fileName);
		}
		if (page > 0){
			page--;
			fileName = projectName + "_" + page + ".csv";
			file = new File("home" + File.separator + projectName + "/" + fileName);
			file.delete();
		}

		while (true) {
			String tempQuery = "?jqlQuery=project+%3D+{0}+ORDER+BY+key+ASC&issuetype=Bug&tempMax=1000&pager/start={1}";
			tempQuery = tempQuery.replace("{0}", projectName);
			tempQuery = tempQuery.replace("{1}", ((page) * 1000) + "");
			if (totalePages >= (page + 1))
				pw.println("Download Jira issues. Page: " + (page + 1) + "/" + totalePages);
			try {
				url = new URL(jiraURL + tempQuery);
				connection = url.openConnection();
				d = parseXML(connection.getInputStream());

				NodeList descNodes = d.getElementsByTagName("item");
				if (descNodes.getLength() == 0)
					return;
				fileName = projectName + "_" + page + ".csv";
				file = new File("home" + File.separator + fileName);
				if (file.exists() && !file.isDirectory()) {
					return;
				}
				PrintWriter pw = null;
				try {
					pw = new PrintWriter(file);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				printHeader(pw);
				printIssuesOfPage(d, pw);
				pw.close();
				page++;
			} catch (Exception e) {
				pw.println(e.getMessage());
				pw.println(("Retrying in 1 minute"));
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				printIssues();
			}
		}
	}

	private void printHeader(PrintWriter pw){
		String header = "issueKey;title;resolution;status;assignee;createdDateEpoch;resolvedDateEpoch;type;attachments;brokenBy;description;comments;";
		pw.println(header);
	}

	private String getTextFromHtml(String str) {
		return Jsoup.parse(str).body().text()
				.replace(";", ".")
				.replace("\n", "")
				.replace("\r", "")
				.replace("\t", "");
	}


	/**
	 *
	 * @param doc
	 * @param pw
	 * @return
	 */
	private void printIssuesOfPage(Document doc, PrintWriter pw) {
		NodeList descNodes = doc.getElementsByTagName("item");
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
		for (int i = 0; i < descNodes.getLength(); i++) {
			Node node = descNodes.item(i);
			String issueKey = "";
			String title = "";
            String description = "";
			String resolution = "";
			String status = "";
			String assignee = "";
			String type = "";
			long createdDateEpoch = 0;
			long resolvedDateEpoch = 0;
            List<String> brokenBy = new LinkedList<String>();
			NodeList children = node.getChildNodes();
			List<String> attachmentsList = new LinkedList<String>();
			String comments = "";
			for (int p = 0; p < children.getLength(); p++) {
				switch (children.item(p).getNodeName()) {
					case "title":
						title = children.item(p).getTextContent().replace(";", "");
						break;
					case "resolution":
						resolution = children.item(p).getTextContent();
						break;
					case "key":
						issueKey = children.item(p).getTextContent();
						break;
					case "created":
						String createdDate = children.item(p).getTextContent();
						try {
							createdDateEpoch = sdf.parse(createdDate).getTime();
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						break;
					case "resolved":
						String resolveddDate = children.item(p).getTextContent();
						try {
							resolvedDateEpoch = sdf.parse(resolveddDate).getTime();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					case "status":
						status = children.item(p).getTextContent();
						break;
					case "assignee":
						assignee = children.item(p).getTextContent();
						break;
					case "comments":
						comments = getTextFromHtml(children.item(p).getTextContent());
						break;
					case "attachments":
						NodeList attachments = children.item(p).getChildNodes();
						for (int u = 0; u < attachments.getLength(); u++) {
							Node attachment = attachments.item(u);
							NamedNodeMap attchmentName = attachment.getAttributes();
							if (attchmentName != null) {
								String att = attchmentName.getNamedItem("name").getNodeValue();
								attachmentsList.add(att);
							}
						}
						break;
					case "type":
						type = children.item(p).getTextContent();
						break;
					case "issuelinks": {
						NodeList issueLinkTypes = children.item(p).getChildNodes();
						for (int t = 0; t < issueLinkTypes.getLength(); t++) {
							NodeList nodes = issueLinkTypes.item(t).getChildNodes();
							for (int n = 0; n < nodes.getLength(); n++) {
								Node issueLinkTypeChild = nodes.item(n);
								if (issueLinkTypeChild.getAttributes() != null
										&& issueLinkTypeChild.getAttributes().getNamedItem("description") != null
										&& issueLinkTypeChild.getAttributes().getNamedItem("description").getNodeValue().equals("is broken by")
								) {
									NodeList brokenByLinks = issueLinkTypeChild.getChildNodes();
									for (int b = 0; b < brokenByLinks.getLength(); b++) {
										Node brokenByLinksNode = brokenByLinks.item(b);
										if (Objects.equals(brokenByLinksNode.getNodeName(), "issuelink")) {
											brokenBy.add(brokenByLinksNode.getChildNodes().item(1).getTextContent());
										}
									}
								}
							}
						}
						break;
					}
                    case "description": {
                        description = getTextFromHtml(children.item(p).getTextContent());
                        break;
                    }
				}
			}
            String toPrint = issueKey + ";" + title + ";" + resolution + ";" + status + ";" + assignee + ";" + createdDateEpoch + ";" + resolvedDateEpoch
                + ";" + type + ";" + attachmentsList + ";" + brokenBy + ";[" + description + "];[" + comments + "];";
			pw.println(toPrint);
		}
	}
}
