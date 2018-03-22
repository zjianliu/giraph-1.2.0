package org.apache.giraph.io.formats;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.util.ArrayList;

public class KMeansTextVertexInputFormat extends TextVertexInputFormat<Text, Text, Text> {


    @Override
    public TextVertexReader createVertexReader(
            InputSplit split, TaskAttemptContext context) throws IOException {
        return new TreeRecordTextReader();
    }

    public class TreeRecordTextReader extends TextVertexReader {

        @Override
        public boolean nextVertex() throws IOException, InterruptedException {
            return getRecordReader().nextKeyValue();
        }

        @Override
        public Vertex<Text, Text, Text> getCurrentVertex() throws IOException,
                InterruptedException {
            String line = getRecordReader().getCurrentValue().toString();

            int firstComma = line.indexOf(',');

            Text id = new Text(line.substring(0, firstComma));

            Text value = new Text(line.substring(firstComma + 1));

            Iterable<Edge<Text, Text>> edgeIdList = new ArrayList<Edge<Text, Text>>();

            Vertex<Text, Text, Text> vertex = getConf().createVertex();

            vertex.initialize(id, value, edgeIdList);
            return vertex;
        }
    }

}
