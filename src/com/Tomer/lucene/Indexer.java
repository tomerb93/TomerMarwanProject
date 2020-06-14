package com.Tomer.lucene;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.json.simple.JSONObject;
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
				keyvalue.keySet().forEach(keyStrInner -> {
					switch ((String) keyStrInner) {
					case LuceneConstants.DATA:
						doc.add(new TextField(LuceneConstants.CONTENTS, keyvalue.get(keyStrInner).toString(),
								Field.Store.NO));
						break;
					case LuceneConstants.SECOND_TITLE:
						doc.add(new StringField(LuceneConstants.SECOND_TITLE, (String) keyvalue.get(keyStrInner),
								Field.Store.NO));
						break;
					case LuceneConstants.CAPTION:
						doc.add(new StringField(LuceneConstants.CAPTION, (String) keyvalue.get(keyStrInner),
								Field.Store.NO));
						break;
					case LuceneConstants.TITLE:
						var titleField = new StringField(LuceneConstants.TITLE, keyvalue.get(keyStrInner).toString(),
								Field.Store.NO);
						doc.add(titleField);
						break;
					case LuceneConstants.PG_TITLE:
						doc.add(new StringField(LuceneConstants.PG_TITLE, (String) keyvalue.get(keyStrInner),
								Field.Store.NO));
						break;
					}
				});
				try {
					writer.addDocument(doc);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

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