package com.Tomer.lucene;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

public class Indexer {
	private IndexWriter writer;

	public Indexer(String indexDirectoryPath) throws IOException {
		// this directory will contain the indexes

		Analyzer enAnalyzer = new EnglishAnalyzer();
		Similarity tfidfSim = new ClassicSimilarity();
		IndexWriterConfig conf = new IndexWriterConfig(enAnalyzer);
		conf.setSimilarity(tfidfSim);
		Directory dir = FSDirectory.open(new File(indexDirectoryPath).toPath());
		// incremental indexing
		conf.setOpenMode(OpenMode.CREATE);
		// create the indexer
		writer = new IndexWriter(dir, conf);

	}

	public void close() throws CorruptIndexException, IOException {
		writer.close();
	}

	private Document addDocument(JSONArray jsonObjects) throws IOException {
		Document document = new Document();

//		document.add(new StringField(LuceneConstants.FILE_NAME, file.getName(), Store.YES));
//		document.add(new TextField(LuceneConstants.CONTENTS, new FileReader(file)));
//		document.add(new StringField(LuceneConstants.FILE_PATH, file.getCanonicalPath(), Store.YES));
		// index file path
		return document;
	}

	private Object parseJSONFile(File file) throws IOException, org.json.simple.parser.ParseException {

		JSONParser parser = new JSONParser();
		// Get the JSON file, in this case is in ~/resources/test.json
		InputStream jsonFile = getClass().getClassLoader().getResourceAsStream(file.getName());
		Reader readerJson = new InputStreamReader(jsonFile);

		// Parse the JSON file using simple-JSON library
		return parser.parse(readerJson);

	}

	@SuppressWarnings("unchecked")
	private void indexFile(File file) throws IOException {
		System.out.println("Indexing " + file.getCanonicalPath());
		try {

			JSONObject jsonObject = (JSONObject) parseJSONFile(file);

			jsonObject.keySet().forEach(keyStr -> {
				Document doc = new Document();
				JSONObject keyvalue = (JSONObject) jsonObject.get(keyStr);
				doc.add(new StringField(LuceneConstants.TABLE_NAME, (String) keyStr, Field.Store.YES));
				//doc.add(new TextField(LuceneConstants.CONTENTS, (String) keyvalue.toString(), Field.Store.YES));
				keyvalue.keySet().forEach(keyStrInner -> {
					switch ((String) keyStrInner) {
//					case "numHeaderRows":
//						doc.add(new StringField((String) keyStrInner, (String) keyvalue.get(keyStrInner),
//								Field.Store.YES));
//						break;
					case "data":
						doc.add(new TextField(LuceneConstants.CONTENTS, keyvalue.get(keyStrInner).toString(),
								Field.Store.NO));
						break;
					case "secondTitle":
						doc.add(new StringField((String) keyStrInner, (String) keyvalue.get(keyStrInner),
								Field.Store.YES));
						break;
					case "caption":
						doc.add(new StringField((String) keyStrInner, (String) keyvalue.get(keyStrInner),
								Field.Store.YES));
						break;
//					case "numericColumns":
//						doc.add(new StringField((String) keyStrInner, (String) keyvalue.get(keyStrInner),
//								Field.Store.YES));
//						break;
					case "title":
						doc.add(new StringField((String) keyStrInner, keyvalue.get(keyStrInner).toString(),
								Field.Store.YES));
						break;
//					case "numDataRows":
//						doc.add(new StringField((String) keyStrInner, (String) keyvalue.get(keyStrInner),
//								Field.Store.YES));
//						break;
//					case "numCols":
//						doc.add(new StringField((String) keyStrInner, (String) keyvalue.get(keyStrInner),
//								Field.Store.YES));
//						break;
					case "pgTitle":
						doc.add(new StringField((String) keyStrInner, (String) keyvalue.get(keyStrInner),
								Field.Store.YES));
						break;
					default:
//						System.out.println("Undefined field");
					}
				});
				try {
//					System.out.println(doc);
					writer.addDocument(doc);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			// Document document = addDocument(jsonObjects);
			// writer.addDocument(document);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}
	}

	public int createIndex(String dataDirPath, FileFilter filter) throws IOException {
		// get all files in the data directory
		File[] files = new File(dataDirPath).listFiles();
		for (File file : files) {
			if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
				indexFile(file);
			}
		}
		return writer.getDocStats().numDocs;
	}

}