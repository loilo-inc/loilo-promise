package tv.loilo.promise.http;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HttpContentDispositionTest {

    //see http://greenbytes.de/tech/tc2231/#attwithfn2231utf8

    //@DataPoints
    public static Fixture[] FIXTURES = {
            new Fixture(true, "", null),
            new Fixture(true, "inline", new Expected("inline", null, null, null)),
            new Fixture(true, "inline; filename=\"foo.html\"", new Expected("inline", "foo.html", null, null)),
            new Fixture(true, "inline; filename=\"Not an attachment!\"", new Expected("inline", "Not an attachment!", null, null)),
            new Fixture(true, "inline; filename=\"foo.pdf\"", new Expected("inline", "foo.pdf", null, null)),
            new Fixture(true, "attachment", new Expected("attachment", null, null, null)),
            new Fixture(true, "ATTACHMENT", new Expected("attachment", null, null, null)),
            new Fixture(true, "attachment; filename=\"foo.html\"", new Expected("attachment", "foo.html", null, null)),
            new Fixture(true, "attachment; filename=\"0000000000111111111122222\"", new Expected("attachment", "0000000000111111111122222", null, null)),
            new Fixture(true, "attachment; filename=\"00000000001111111111222222222233333\"", new Expected("attachment", "00000000001111111111222222222233333", null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename=\"f\\oo.html\"", new Expected("attachment", "f\\oo.html", null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename=\"\\\"quoting\\\" tested.html\"", new Expected("attachment", "\"quoting\" tested.html", null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename=\"Here's a semicolon;.html\"", new Expected("attachment", "Here's a semicolon;.html", null, null)),
            new Fixture(true, "attachment; foo=\"bar\"; filename=\"foo.html\"", new Expected("attachment", "foo.html", null, null)),
            new Fixture(true, "attachment; foo=\"\\\"\\\\\";filename=\"foo.html\"", new Expected("attachment", "foo.html", null, null)),
            new Fixture(true, "attachment; FILENAME=\"foo.html\"", new Expected("attachment", "foo.html", null, null)),
            new Fixture(true, "attachment; filename=foo.html", new Expected("attachment", "foo.html", null, null)),
            new Fixture(true, "attachment; filename='foo.bar'", new Expected("attachment", "foo.bar", null, null)),
            new Fixture(true, "attachment; filename=\"foo-ä.html\"", new Expected("attachment", "foo-ä.html", null, null)),
            new Fixture(true, "attachment; filename=\"foo-Ã¤.html\"", new Expected("attachment", "foo-Ã¤.html", null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename=\"foo-%41.html\"", new Expected("attachment", "foo-%41.html", null, null)),
            new Fixture(true, "attachment; filename=\"50%.html\"", new Expected("attachment", "50%.html", null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename=\"foo-%\\41.html\"", new Expected("attachment", "foo-%41.html", null, null)),
            new Fixture(true, "attachment; name=\"foo-%41.html\"", new Expected("attachment", null, null, "foo-%41.html")),
            new Fixture(/* 失敗していい */false, "attachment; filename=\"ä-%41.html\"", new Expected("attachment", "ä-%41.html", null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename=\"foo-%c3%a4-%e2%82%ac.html\"", new Expected("attachment", "foo-%c3%a4-%e2%82%ac.html", null, null)),
            new Fixture(true, "attachment; filename =\"foo.html\"", new Expected("attachment", "foo.html", null, null)),
            new Fixture(true, "attachment; xfilename=foo.html", new Expected("attachment", "foo.html", null, null)),
            new Fixture(true, "attachment; filename=\"/foo.html\"", new Expected("attachment", "/foo.html", null, null)),
            new Fixture(true, "attachment; filename=\"\\\\foo.html\"", new Expected("attachment", "\\foo.html", null, null)),
            new Fixture(true, "attachment; creation-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"", new Expected("attachment", null, null, null)),
            new Fixture(true, "attachment; modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"", new Expected("attachment", null, null, null)),
            new Fixture(/* 失敗していい */true, "foobar", new Expected("foobar", null, null, null)),
            new Fixture(true, "attachment; example=\"filename=example.txt\"", new Expected("attachment", null, null, null)),
            new Fixture(true, "attachment; filename*=iso-8859-1''foo-%E4.html", new Expected("attachment", null, "foo-ä.html", null)),
            new Fixture(true, "attachment; filename*=UTF-8''foo-%c3%a4-%e2%82%ac.html", new Expected("attachment", null, "foo-ä-€.html", null)),
            new Fixture(true, "attachment; filename*=''foo-%c3%a4-%e2%82%ac.html", new Expected("attachment", null, "foo-%c3%a4-%e2%82%ac.html", null)),
            new Fixture(true, "attachment; filename*=UTF-8''foo-a%cc%88.html", new Expected("attachment", null, "foo-ä.html", null)),
            new Fixture(true, "attachment; filename*= UTF-8''foo-%c3%a4.html", new Expected("attachment", null, "foo-ä.html", null)),
            new Fixture(true, "attachment; filename* =UTF-8''foo-%c3%a4.html", new Expected("attachment", null, "foo-ä.html", null)),
            new Fixture(true, "attachment; filename*=UTF-8''A-%2541.html", new Expected("attachment", null, "A-%41.html", null)),
            new Fixture(true, "attachment; filename*=UTF-8''%5cfoo.html", new Expected("attachment", null, "\\foo.html", null)),
            new Fixture(/* 失敗していい */false, "attachment; filename*0=\"foo.\"; filename*1=\"html\"", new Expected("attachment", null, null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename*0=\"foo\"; filename*1=\"\\b\\a\\r.html\"", new Expected("attachment", null, null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename*0*=UTF-8''foo-%c3%a4; filename*1=\".html\"", new Expected("attachment", null, null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename*0=\"foo\"; filename*01=\"bar\"", new Expected("attachment", null, null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename*0=\"foo\"; filename*2=\"bar\"", new Expected("attachment", null, null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename*1=\"foo.\"; filename*2=\"html\"", new Expected("attachment", null, null, null)),
            new Fixture(/* 失敗していい */false, "attachment; filename*1=\"bar\"; filename*0=\"foo\"", new Expected("attachment", null, null, null)),
            new Fixture(true, "attachment; filename*=UTF-8''foo-%c3%a4.html; filename=\"foo-ae.html\"", new Expected("attachment", "foo-ae.html", "foo-ä.html", null)),
            new Fixture(true, "attachment; filename=\"foo-ae.html\"; filename*=UTF-8''foo-%c3%a4.html", new Expected("attachment", "foo-ae.html", "foo-ä.html", null)),
            new Fixture(true, "attachment; filename*0*=ISO-8859-15''euro-sign%3d%a4; filename*=ISO-8859-1''currency-sign%3d%a4", new Expected("attachment", null, "currency-sign=¤", null)),
            new Fixture(true, "attachment; foobar=x; filename=\"foo.html\"", new Expected("attachment", "foo.html", null, null)),
            new Fixture(true, "attachment; filename=\"=?ISO-8859-1?Q?foo-=E4.html?=\"", new Expected("attachment", "=?ISO-8859-1?Q?foo-=E4.html?=", null, null))
    };

    @Test
    public void testTryParse() throws Exception {
        for(final Fixture fixture : FIXTURES){
            testFixture(fixture);
        }
    }

    public void testFixture(final Fixture fixture) throws Exception {
        if (!fixture.mEnable) {
            return;
        }
        final HttpContentDisposition result = HttpContentDisposition.tryParse(fixture.mInput);
        if (fixture.mExpected == null) {
            assertThat(result, nullValue());
        } else {
            assertThat(result, notNullValue());
            assertThat(result.getDispositionType(), equalTo(fixture.mExpected.mDispositionType));
            assertThat(result.getFileName(), equalTo(fixture.mExpected.mFileName));
            assertThat(result.getFileNameStar(), equalTo(fixture.mExpected.mFileNameStar));
            assertThat(result.getName(), equalTo(fixture.mExpected.mName));
        }
    }

    public static class Expected {
        private final String mDispositionType;
        private final String mFileName;
        private final String mFileNameStar;
        private final String mName;

        public Expected(final String dispositionType, final String fileName, final String fileNameStar, final String name) {
            mDispositionType = dispositionType;
            mFileName = fileName;
            mFileNameStar = fileNameStar;
            mName = name;
        }
    }

    public static class Fixture {
        private final boolean mEnable;
        private final String mInput;
        private final Expected mExpected;

        public Fixture(final boolean enable, final String input, final Expected expected) {
            mEnable = enable;
            mInput = input;
            mExpected = expected;
        }
    }
}