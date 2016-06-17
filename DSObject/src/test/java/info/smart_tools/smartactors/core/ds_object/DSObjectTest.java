package info.smart_tools.smartactors.core.ds_object;

import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Tests for DSObject
 */
public class DSObjectTest {

    @Test
    public void checkCreationByEmptyConstructor() {
        IObject obj = new DSObject();
        assertNotNull(obj);
    }

    @Test
    public void checkCreationByString()
            throws Exception {
        IObject obj = new DSObject("{\n" +
                "  \"value\": 1,\n" +
                "  \"string\": \"foo\"\n" +
                "}");
        assertNotNull(obj);
        assertEquals(1, obj.getValue(new FieldName("value")));
        assertEquals("foo", obj.getValue(new FieldName("string")));
    }

    @Test
    public void checkCreationByMap()
            throws Exception {
        IFieldName fieldName = mock(IFieldName.class);
        Object obj = mock(Object.class);
        Map<IFieldName, Object> map = new HashMap<IFieldName, Object>(){{put(fieldName, obj);}};
        IObject result = new DSObject(map);
        assertNotNull(obj);
        assertEquals(obj, result.getValue(fieldName));
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnCreationByString()
            throws Exception {
        String str = null;
        new DSObject(str);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnCreationByMap()
            throws Exception {
        Map<IFieldName, Object> map = null;
        new DSObject(map);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkExceptionOnGetValue()
            throws Exception {
        (new DSObject()).getValue(null);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkExceptionOnSetValue()
            throws Exception {
        (new DSObject()).setValue(null, new Object());
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkExceptionOnDeleteValue()
            throws Exception {
        (new DSObject()).deleteField(null);
        fail();
    }

    @Test
    public void checkSetGetAndDeleteValue()
            throws Exception {
        IFieldName fieldName = mock(IFieldName.class);
        Object value = mock(Object.class);
        IObject obj = new DSObject();
        assertNull(obj.getValue(fieldName));
        obj.setValue(fieldName, value);
        assertEquals(value, obj.getValue(fieldName));
        obj.deleteField(fieldName);
        assertNull(obj.getValue(fieldName));
    }

    @Test
    public void checkSerialization()
            throws Exception {
        String json = "{\"value\":1,\"string\":\"foo\"}";
        IObject obj = new DSObject(json);
        assertEquals(json, obj.serialize());
    }

    @Test (expected = SerializeException.class)
    public void checkExceptionOnSerialization()
            throws Exception {
        String json = "{\"value\":1,\"string\":\"foo\"}";
        IFieldName fieldName = mock(IFieldName.class);
        IObject obj = new DSObject(json);
        obj.setValue(fieldName, Thread.currentThread());
        obj.serialize();
        fail();
    }

    @Test
    public void checkSerializationWithNestedIObject()
            throws Exception {
        IObject obj = new DSObject();
        IFieldName fieldName = mock(IFieldName.class);
        obj.setValue(fieldName, new DSObject());
        String result = obj.serialize();
        assertNotNull(result);
    }

    @Test
    public void checkListObject()
            throws Exception {
        IObject obj = new DSObject(json);

    }


    private String json = "\n" +
            "  {\n" +
            "    \"_id\": \"5763b44313d7282913fed66a\",\n" +
            "    \"index\": 0,\n" +
            "    \"guid\": \"29551173-8fba-4faf-a386-c2d7123ef810\",\n" +
            "    \"isActive\": false,\n" +
            "    \"balance\": \"$1,348.07\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 39,\n" +
            "    \"eyeColor\": \"green\",\n" +
            "    \"name\": {\n" +
            "      \"first\": \"Nelda\",\n" +
            "      \"last\": \"Hale\"\n" +
            "    },\n" +
            "    \"company\": \"PRIMORDIA\",\n" +
            "    \"email\": \"nelda.hale@primordia.us\",\n" +
            "    \"phone\": \"+1 (914) 540-3888\",\n" +
            "    \"address\": \"401 Irving Avenue, Grazierville, Maine, 7469\",\n" +
            "    \"about\": \"Magna deserunt consequat ex do qui qui commodo magna consectetur cillum est elit laboris excepteur. Exercitation anim officia excepteur tempor id irure. Nisi esse cupidatat do officia tempor dolore Lorem. Aliquip elit eiusmod laborum sunt nulla id veniam.\",\n" +
            "    \"registered\": \"Thursday, August 21, 2014 6:06 AM\",\n" +
            "    \"latitude\": \"-20.806496\",\n" +
            "    \"longitude\": \"-107.071932\",\n" +
            "    \"tags\": [\n" +
            "      \"laboris\",\n" +
            "      \"aute\",\n" +
            "      \"culpa\",\n" +
            "      \"dolore\",\n" +
            "      \"anim\"\n" +
            "    ],\n" +
            "    \"range\": [\n" +
            "      0,\n" +
            "      1,\n" +
            "      2,\n" +
            "      3,\n" +
            "      4,\n" +
            "      5,\n" +
            "      6,\n" +
            "      7,\n" +
            "      8,\n" +
            "      9\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Aida Collins\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Tia Castillo\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Diana Delgado\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Nelda! You have 6 unread messages.\",\n" +
            "    \"favoriteFruit\": \"strawberry\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"5763b44311adb760a242d9cc\",\n" +
            "    \"index\": 1,\n" +
            "    \"guid\": \"98e3302c-d58e-42d6-a5e4-464b688e9817\",\n" +
            "    \"isActive\": true,\n" +
            "    \"balance\": \"$1,638.22\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 32,\n" +
            "    \"eyeColor\": \"green\",\n" +
            "    \"name\": {\n" +
            "      \"first\": \"Bentley\",\n" +
            "      \"last\": \"Ingram\"\n" +
            "    },\n" +
            "    \"company\": \"VIDTO\",\n" +
            "    \"email\": \"bentley.ingram@vidto.name\",\n" +
            "    \"phone\": \"+1 (881) 481-3467\",\n" +
            "    \"address\": \"110 Keen Court, Nelson, Connecticut, 5761\",\n" +
            "    \"about\": \"Velit culpa aute velit aute nulla ad id. Cillum eiusmod duis tempor enim sint commodo amet nisi eu elit deserunt mollit sint. Cupidatat consectetur amet reprehenderit ea consequat nisi ad minim do sunt occaecat ad sint. In ipsum mollit non culpa amet excepteur eiusmod elit nostrud amet.\",\n" +
            "    \"registered\": \"Wednesday, August 19, 2015 2:54 PM\",\n" +
            "    \"latitude\": \"-54.828854\",\n" +
            "    \"longitude\": \"8.748376\",\n" +
            "    \"tags\": [\n" +
            "      \"mollit\",\n" +
            "      \"dolore\",\n" +
            "      \"fugiat\",\n" +
            "      \"reprehenderit\",\n" +
            "      \"dolor\"\n" +
            "    ],\n" +
            "    \"range\": [\n" +
            "      0,\n" +
            "      1,\n" +
            "      2,\n" +
            "      3,\n" +
            "      4,\n" +
            "      5,\n" +
            "      6,\n" +
            "      7,\n" +
            "      8,\n" +
            "      9\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Santos Lowery\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Julie Blevins\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Tanisha Huber\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Bentley! You have 8 unread messages.\",\n" +
            "    \"favoriteFruit\": \"strawberry\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"5763b443a4822482f164962b\",\n" +
            "    \"index\": 2,\n" +
            "    \"guid\": \"22e37475-57e5-4674-ab83-126e3260b3ec\",\n" +
            "    \"isActive\": true,\n" +
            "    \"balance\": \"$1,817.75\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 23,\n" +
            "    \"eyeColor\": \"brown\",\n" +
            "    \"name\": {\n" +
            "      \"first\": \"Aurelia\",\n" +
            "      \"last\": \"Emerson\"\n" +
            "    },\n" +
            "    \"company\": \"YOGASM\",\n" +
            "    \"email\": \"aurelia.emerson@yogasm.me\",\n" +
            "    \"phone\": \"+1 (983) 551-2110\",\n" +
            "    \"address\": \"156 Frank Court, Floris, California, 9631\",\n" +
            "    \"about\": \"Ea consectetur sunt in enim est proident consectetur et velit aute consectetur anim. Id ad exercitation culpa ut ipsum fugiat pariatur incididunt minim. Sit amet Lorem voluptate Lorem ipsum Lorem nulla dolore amet ut. Dolore ea amet fugiat ea anim culpa anim eu est do deserunt magna proident. Aute adipisicing nulla irure laboris cupidatat magna minim deserunt.\",\n" +
            "    \"registered\": \"Saturday, December 12, 2015 7:57 PM\",\n" +
            "    \"latitude\": \"45.206576\",\n" +
            "    \"longitude\": \"137.669175\",\n" +
            "    \"tags\": [\n" +
            "      \"sit\",\n" +
            "      \"anim\",\n" +
            "      \"nisi\",\n" +
            "      \"pariatur\",\n" +
            "      \"deserunt\"\n" +
            "    ],\n" +
            "    \"range\": [\n" +
            "      0,\n" +
            "      1,\n" +
            "      2,\n" +
            "      3,\n" +
            "      4,\n" +
            "      5,\n" +
            "      6,\n" +
            "      7,\n" +
            "      8,\n" +
            "      9\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Janet Gallegos\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Gail Orr\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Diann Madden\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Aurelia! You have 8 unread messages.\",\n" +
            "    \"favoriteFruit\": \"banana\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"5763b44303144d74d0a60186\",\n" +
            "    \"index\": 3,\n" +
            "    \"guid\": \"6a568385-777d-4bc8-8c41-81c17d3373e2\",\n" +
            "    \"isActive\": false,\n" +
            "    \"balance\": \"$1,228.82\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 34,\n" +
            "    \"eyeColor\": \"blue\",\n" +
            "    \"name\": {\n" +
            "      \"first\": \"Joyner\",\n" +
            "      \"last\": \"Goff\"\n" +
            "    },\n" +
            "    \"company\": \"BOILICON\",\n" +
            "    \"email\": \"joyner.goff@boilicon.biz\",\n" +
            "    \"phone\": \"+1 (854) 569-3806\",\n" +
            "    \"address\": \"153 Dahill Road, Clarksburg, Palau, 5726\",\n" +
            "    \"about\": \"Est nisi magna adipisicing fugiat voluptate ut sint pariatur amet. Fugiat ipsum in qui reprehenderit exercitation est non laborum ad sint. Veniam ipsum non in labore nostrud cillum sunt ipsum veniam ullamco ex ut. Exercitation fugiat anim nulla labore mollit enim. Ullamco fugiat cillum nulla voluptate eu dolore.\",\n" +
            "    \"registered\": \"Wednesday, December 10, 2014 11:06 PM\",\n" +
            "    \"latitude\": \"-32.049376\",\n" +
            "    \"longitude\": \"-175.95743\",\n" +
            "    \"tags\": [\n" +
            "      \"ea\",\n" +
            "      \"est\",\n" +
            "      \"amet\",\n" +
            "      \"incididunt\",\n" +
            "      \"id\"\n" +
            "    ],\n" +
            "    \"range\": [\n" +
            "      0,\n" +
            "      1,\n" +
            "      2,\n" +
            "      3,\n" +
            "      4,\n" +
            "      5,\n" +
            "      6,\n" +
            "      7,\n" +
            "      8,\n" +
            "      9\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Corrine Burnett\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Alba Carr\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Marisa Dalton\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Joyner! You have 7 unread messages.\",\n" +
            "    \"favoriteFruit\": \"banana\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"5763b44343e7e6a3554a7124\",\n" +
            "    \"index\": 4,\n" +
            "    \"guid\": \"d5b12386-18fe-42b1-a6ea-6ed99c6360a2\",\n" +
            "    \"isActive\": false,\n" +
            "    \"balance\": \"$2,115.41\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 40,\n" +
            "    \"eyeColor\": \"blue\",\n" +
            "    \"name\": {\n" +
            "      \"first\": \"Isabelle\",\n" +
            "      \"last\": \"Greene\"\n" +
            "    },\n" +
            "    \"company\": \"ZILLACOM\",\n" +
            "    \"email\": \"isabelle.greene@zillacom.biz\",\n" +
            "    \"phone\": \"+1 (895) 541-3152\",\n" +
            "    \"address\": \"800 Vermont Court, Longbranch, New Hampshire, 4426\",\n" +
            "    \"about\": \"Nisi voluptate do velit ex voluptate magna cupidatat incididunt velit adipisicing ut sint. Commodo deserunt proident excepteur dolor. Dolor et deserunt esse sit veniam id ex officia magna. In consequat enim nulla reprehenderit excepteur. Aliqua laboris culpa quis proident ad ullamco mollit id occaecat ex ex duis proident.\",\n" +
            "    \"registered\": \"Saturday, November 7, 2015 10:35 AM\",\n" +
            "    \"latitude\": \"88.321386\",\n" +
            "    \"longitude\": \"24.9944\",\n" +
            "    \"tags\": [\n" +
            "      \"id\",\n" +
            "      \"veniam\",\n" +
            "      \"cupidatat\",\n" +
            "      \"dolore\",\n" +
            "      \"amet\"\n" +
            "    ],\n" +
            "    \"range\": [\n" +
            "      0,\n" +
            "      1,\n" +
            "      2,\n" +
            "      3,\n" +
            "      4,\n" +
            "      5,\n" +
            "      6,\n" +
            "      7,\n" +
            "      8,\n" +
            "      9\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Hickman Norton\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Merrill Shaffer\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Wilda Taylor\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Isabelle! You have 8 unread messages.\",\n" +
            "    \"favoriteFruit\": \"strawberry\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"5763b443a21f5e87721709f1\",\n" +
            "    \"index\": 5,\n" +
            "    \"guid\": \"796bde82-d6ed-4645-a4a5-74b6e501a663\",\n" +
            "    \"isActive\": false,\n" +
            "    \"balance\": \"$1,624.41\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 24,\n" +
            "    \"eyeColor\": \"green\",\n" +
            "    \"name\": {\n" +
            "      \"first\": \"Casandra\",\n" +
            "      \"last\": \"Heath\"\n" +
            "    },\n" +
            "    \"company\": \"VIXO\",\n" +
            "    \"email\": \"casandra.heath@vixo.com\",\n" +
            "    \"phone\": \"+1 (920) 595-2489\",\n" +
            "    \"address\": \"671 Love Lane, Zeba, North Carolina, 6906\",\n" +
            "    \"about\": \"Irure consectetur ex ipsum ipsum commodo qui id fugiat. Sit velit ad exercitation irure ullamco dolor anim Lorem reprehenderit voluptate aute. Aliqua ipsum commodo Lorem eu incididunt proident enim commodo consectetur sit aliqua adipisicing fugiat irure. Tempor sint dolore consectetur labore do eu ea dolor sint dolor ex laboris.\",\n" +
            "    \"registered\": \"Monday, January 20, 2014 1:43 AM\",\n" +
            "    \"latitude\": \"-88.665766\",\n" +
            "    \"longitude\": \"-95.22056\",\n" +
            "    \"tags\": [\n" +
            "      \"consequat\",\n" +
            "      \"minim\",\n" +
            "      \"deserunt\",\n" +
            "      \"anim\",\n" +
            "      \"anim\"\n" +
            "    ],\n" +
            "    \"range\": [\n" +
            "      0,\n" +
            "      1,\n" +
            "      2,\n" +
            "      3,\n" +
            "      4,\n" +
            "      5,\n" +
            "      6,\n" +
            "      7,\n" +
            "      8,\n" +
            "      9\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Lara Lamb\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Carroll Hurst\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Fischer Cooke\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Casandra! You have 8 unread messages.\",\n" +
            "    \"favoriteFruit\": \"banana\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"_id\": \"5763b4437eb7650cedac24ad\",\n" +
            "    \"index\": 6,\n" +
            "    \"guid\": \"2c7b3902-063b-4df7-9f1f-e79bd3322b5b\",\n" +
            "    \"isActive\": false,\n" +
            "    \"balance\": \"$1,043.65\",\n" +
            "    \"picture\": \"http://placehold.it/32x32\",\n" +
            "    \"age\": 37,\n" +
            "    \"eyeColor\": \"blue\",\n" +
            "    \"name\": {\n" +
            "      \"first\": \"Graham\",\n" +
            "      \"last\": \"Reid\"\n" +
            "    },\n" +
            "    \"company\": \"SULFAX\",\n" +
            "    \"email\": \"graham.reid@sulfax.info\",\n" +
            "    \"phone\": \"+1 (876) 400-3471\",\n" +
            "    \"address\": \"760 Cook Street, Cataract, Florida, 9971\",\n" +
            "    \"about\": \"Deserunt magna qui ut elit. Fugiat anim proident quis exercitation et occaecat. Sunt consequat do commodo minim duis ex irure reprehenderit pariatur et voluptate adipisicing consequat. Incididunt laborum dolore consectetur cupidatat.\",\n" +
            "    \"registered\": \"Monday, January 19, 2015 1:11 AM\",\n" +
            "    \"latitude\": \"-48.190744\",\n" +
            "    \"longitude\": \"-44.79573\",\n" +
            "    \"tags\": [\n" +
            "      \"sunt\",\n" +
            "      \"anim\",\n" +
            "      \"magna\",\n" +
            "      \"pariatur\",\n" +
            "      \"magna\"\n" +
            "    ],\n" +
            "    \"range\": [\n" +
            "      0,\n" +
            "      1,\n" +
            "      2,\n" +
            "      3,\n" +
            "      4,\n" +
            "      5,\n" +
            "      6,\n" +
            "      7,\n" +
            "      8,\n" +
            "      9\n" +
            "    ],\n" +
            "    \"friends\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"name\": \"Morgan Sullivan\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 1,\n" +
            "        \"name\": \"Marquez Fisher\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 2,\n" +
            "        \"name\": \"Erna Flynn\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"greeting\": \"Hello, Graham! You have 6 unread messages.\",\n" +
            "    \"favoriteFruit\": \"strawberry\"\n" +
            "  }\n" +
            "";
}
