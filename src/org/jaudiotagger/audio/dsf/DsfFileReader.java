/*
 * Created on 03.05.2015
 * Author: Veselin Markov.
 */
package org.jaudiotagger.audio.dsf;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.generic.AudioFileReader3;
import org.jaudiotagger.audio.generic.DataSource;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.audio.iff.IffHeaderChunk;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v22Tag;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import static org.jaudiotagger.audio.dsf.DsdChunk.CHUNKSIZE_LENGTH;

/**
 * Reads the ID3 Tags as specified by <a href=
 * "http://dsd-guide.com/sites/default/files/white-papers/DSFFileFormatSpec_E.pdf"
 * /> DSFFileFormatSpec_E.pdf </a>.
 *
 * @author Veselin Markov (veselin_m84 a_t yahoo.com)
 */
public class DsfFileReader extends AudioFileReader3
{
    @Override
    protected GenericAudioHeader getEncodingInfo(DataSource dataSource) throws CannotReadException, IOException
    {

        DsdChunk dsd = DsdChunk.readChunk(Utils.readFileDataIntoBufferLE(dataSource, DsdChunk.DSD_HEADER_LENGTH));
        if (dsd != null)
        {
            ByteBuffer fmtChunkBuffer = Utils.readFileDataIntoBufferLE(dataSource, IffHeaderChunk.SIGNATURE_LENGTH + CHUNKSIZE_LENGTH);
            FmtChunk fmt = FmtChunk.readChunkHeader(fmtChunkBuffer);
            if (fmt != null)
            {
                return fmt.readChunkData(dsd, dataSource);
            }
            else
            {
                throw new CannotReadException("Not a valid dsf file. Content does not include 'fmt ' chunk. DataSource:" + dataSource);
            }
        }
        else
        {
            throw new CannotReadException("Not a valid dsf file. Content does not start with 'DSD '. DataSource:" + dataSource);
        }

    }

    @Override
    protected Tag getTag(DataSource dataSource) throws CannotReadException, IOException
    {

        DsdChunk dsd = DsdChunk.readChunk(Utils.readFileDataIntoBufferLE(dataSource, DsdChunk.DSD_HEADER_LENGTH));
        if (dsd != null)
        {
            return readTag(dataSource, dsd);
        }
        else
        {
            throw new CannotReadException("Not a valid dsf file. Content does not start with 'DSD '. DataSource:" + dataSource);
        }

    }

    /**
     * Reads the ID3v2 tag starting at the {@code tagOffset} position in the
     * supplied file.
     *
     * @param dataSource the dataSource.
     * @param dsd  the dsd chunk
     * @return the read tag or an empty tag if something went wrong. Never
     * <code>null</code>.
     * @throws IOException if cannot read file.
     */
    private Tag readTag(DataSource dataSource, DsdChunk dsd) throws CannotReadException,IOException
    {
        if(dsd.getMetadataOffset() > 0)
        {
            dataSource.position(dsd.getMetadataOffset());
            ID3Chunk id3Chunk = ID3Chunk.readChunk(Utils.readFileDataIntoBufferLE(dataSource, (int) (dataSource.size() - dataSource.position())));
            if(id3Chunk!=null)
            {
                int version = id3Chunk.getDataBuffer().get(AbstractID3v2Tag.FIELD_TAG_MAJOR_VERSION_POS);
                try
                {
                    switch (version)
                    {
                        case ID3v22Tag.MAJOR_VERSION:
                            return new ID3v22Tag(id3Chunk.getDataBuffer(), "");
                        case ID3v23Tag.MAJOR_VERSION:
                            return new ID3v23Tag(id3Chunk.getDataBuffer(), "");
                        case ID3v24Tag.MAJOR_VERSION:
                            return new ID3v24Tag(id3Chunk.getDataBuffer(), "");
                        default:
                            logger.log(Level.WARNING, "Unknown ID3v2 version " + version + ". Returning an empty ID3v2 Tag. DataSource:" + dataSource);
                            return null;
                    }
                }
                catch (TagException e)
                {
                    throw new CannotReadException("Could not read ID3v2 tag:corruption. DataSource:" + dataSource);
                }
            }
            else
            {
                logger.log(Level.WARNING, "No existing ID3 tag(1). DataSource:" + dataSource);
                return null;
            }
        }
        else
        {
            logger.log(Level.WARNING, "No existing ID3 tag(2). DataSource:" + dataSource);
            return   null;
        }
    }
}
