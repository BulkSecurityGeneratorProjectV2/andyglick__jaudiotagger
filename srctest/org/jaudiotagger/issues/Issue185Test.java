package org.jaudiotagger.issues;

import org.jaudiotagger.AbstractTestCase;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.id3.ID3v22Tag;
import org.jaudiotagger.tag.Tag;

import java.io.FileOutputStream;
import java.io.File;

/**
 * Test Creating Null fields
 */
public class Issue185Test extends AbstractTestCase
{

    public void testDefaultTagMp3()
    {
        Exception exceptionCaught = null;
        try
        {
            File testFile = AbstractTestCase.copyAudioToTmp("testV1.mp3");
            AudioFile af = AudioFileIO.read(testFile);

            //No Tag
            assertNull(af.getTag());

            //Tag Created
            Tag tag = af.createDefaultTag();
            assertTrue(tag instanceof ID3v23Tag);

            //but not set in tag itself
            assertNull(af.getTag());

            //Now set
            af.setTag(tag);
            assertTrue(af.getTag() instanceof ID3v23Tag);

            //Save changes
            af.commit();

            af = AudioFileIO.read(testFile);
            assertTrue(af.getTag() instanceof ID3v23Tag);

        }
        catch (Exception e)
        {
            exceptionCaught = e;
        }
        assertNull(exceptionCaught);
    }


    public void testDefaultTagMp3AndCreate()
    {
        Exception exceptionCaught = null;
        try
        {
            File testFile = AbstractTestCase.copyAudioToTmp("testV1.mp3");
            AudioFile af = AudioFileIO.read(testFile);

            //No Tag
            assertNull(af.getTag());

            //Tag Created and set
            Tag tag = af.getTagOrCreateAndSetDefault();
            assertTrue(tag instanceof ID3v23Tag);
            assertTrue(af.getTag() instanceof ID3v23Tag);

            //Save changes
            af.commit();

            af = AudioFileIO.read(testFile);
            assertTrue(af.getTag() instanceof ID3v23Tag);

        }
        catch (Exception e)
        {
            exceptionCaught = e;
        }
        assertNull(exceptionCaught);
    }
}
