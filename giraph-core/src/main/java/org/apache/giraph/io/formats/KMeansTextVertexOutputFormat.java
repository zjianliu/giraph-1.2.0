package org.apache.giraph.io.formats;

import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;

public class KMeansTextVertexOutputFormat extends TextVertexOutputFormat<Text, Text, Text> {

    @Override
    public TextVertexWriter createVertexWriter(
            TaskAttemptContext context) throws IOException,
            InterruptedException {

        return new TreeRecordTextWriter();
    }

    public class TreeRecordTextWriter extends TextVertexWriter {

        Text newKey = new Text();
        Text newValue = new Text();

        public void writeVertex(
                Vertex<Text, Text, Text> vertex)
                throws IOException, InterruptedException {


            newKey.set(vertex.getId().toString() + "," + vertex.getValue());

            getRecordWriter().write(newKey, newValue);

        }

    }
}