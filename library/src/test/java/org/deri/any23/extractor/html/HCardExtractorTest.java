package org.deri.any23.extractor.html;

import junit.framework.Assert;
import org.deri.any23.RDFHelper;
import org.deri.any23.extractor.ExtractionException;
import org.deri.any23.extractor.ExtractorFactory;
import org.deri.any23.vocab.FOAF;
import org.deri.any23.vocab.VCARD;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * {@link org.deri.any23.extractor.html.HCardExtractor} test case.
 */
//TODO: low - reactivate commented tests.
public class HCardExtractorTest extends AbstractMicroformatTestCase {

     protected ExtractorFactory<?> getExtractorFactory() {
        return HCardExtractor.factory;
    }

    // @Test
	public void testInferredPerson() throws RepositoryException {
		assertExtracts("hcard/23-abbr-title-everything.html");
		assertDefaultVCard();
		assertStatementsSize(FOAF.topic, null, 1);
		Resource card = conn.getStatements(null, RDF.TYPE, VCARD.VCard, false).next().getSubject();
        Resource person =  findObjectAsResource(card, FOAF.topic);

		Assert.assertEquals( findObjectAsLiteral(person, FOAF.name), findObjectAsLiteral(card, VCARD.fn) );
		Assert.assertEquals( findObjectAsLiteral(person, FOAF.name), findObjectAsLiteral(card, VCARD.fn) );
	}

    @Test
	public void testEMailNotUriReal() throws RepositoryException {
		assertExtracts("hcard/17-email-not-uri.html");
		assertDefaultVCard();
		assertJohn();
		assertContains(VCARD.email, RDFHelper.uri("mailto:john@example.com"));
	}

    @Test
    public void testTel() throws RepositoryException {
		assertExtracts("hcard/21-tel.html");
		assertDefaultVCard();
		String[] tels = {
                "+1.415.555.1231", "+1.415.555.1235",
				"+1.415.555.1236", "+1.415.555.1237", "+1.415.555.1238",
				"+1.415.555.1239", "+1.415.555.1240", "+1.415.555.1241",
				"+1.415.555.1242", "+1.415.555.1243"
        };
		for (String tel : tels) {
			assertContains(VCARD.tel, RDFHelper.uri("tel:" + tel));
		}
		Resource telResource = RDFHelper.uri("tel:+14155551233");
		assertContains(VCARD.fax, telResource);
		assertContains(VCARD.workTel, telResource);
		assertContains(VCARD.homeTel, telResource);
		assertJohn();
	}

    @Test
    public void testAbbrTitleEverything() throws RepositoryException {
		assertExtracts("hcard/23-abbr-title-everything.html");
		assertDefaultVCard();

		assertContains(VCARD.fn, "John Doe");
		assertContains(VCARD.nickname, "JJ");

		assertContains(VCARD.given_name, "Jonathan");
		assertContains(VCARD.additional_name, "John");
		assertContains(VCARD.family_name, "Doe-Smith");
		assertContains(VCARD.honorific_suffix, "Medical Doctor");

		assertContains(VCARD.title, "President");
		assertContains(VCARD.role, "Chief");
		assertContains(VCARD.tz, "-0700");
		assertContains(VCARD.bday, "2006-04-04");
		assertContains(VCARD.tel, RDFHelper.uri("tel:415.555.1234"));
		assertContains(VCARD.uid, "abcdefghijklmnopqrstuvwxyz");
		assertContains(VCARD.class_, "public");
		assertContains(VCARD.note, "this is a note");
		assertContains(VCARD.organization_name, "Intellicorp");
		assertContains(VCARD.organization_unit, "Intelligence");
		
		// We define the property in this extractor _but_ we do not parse it.
		assertContains(VCARD.geo, (Resource) null);
		// Thus we do not cointain these.
		// The interaction is in @link RDFMergerTest.java
		assertNotContains(RDF.TYPE, VCARD.Location);
		assertNotContains(null, VCARD.latitude, "37.77");
		assertNotContains(null, VCARD.longitude, "-122.41");

		//see above
		assertContains(VCARD.adr, (Resource) null);
		assertNotContains(RDF.TYPE, VCARD.Address);
		assertNotContains(null, VCARD.post_office_box, "Box 1234");
		assertNotContains(null, VCARD.extended_address, "Suite 100");
		assertNotContains(null, VCARD.street_address, "123 Fake Street");
		assertNotContains(null, VCARD.locality, "San Francisco");
		assertNotContains(null, VCARD.region, "California");
		assertNotContains(null, VCARD.postal_code, "12345-6789");
		assertNotContains(null, VCARD.country_name, "United States of America");
		assertNotContains(null, VCARD.addressType, "work");
	}

    @Test
    public void testGeoAbbr() throws RepositoryException {
		assertExtracts("hcard/25-geo-abbr.html");
		assertModelNotEmpty();
		assertContains(VCARD.fn, "Paradise");
         assertContains(RDF.TYPE, VCARD.Organization);
		 assertContains(VCARD.organization_name, "Paradise");
		// See above: geo property yes, gteo blank node no.
		assertContains(VCARD.geo, (Resource) null);
		assertNotContains(RDF.TYPE, VCARD.Location);
		assertNotContains(null, VCARD.latitude, "30.267991");
		assertNotContains(null, VCARD.longitude, "-97.739568");
	}

    @Test
    public void testAncestors() throws RepositoryException {
		assertExtracts("hcard/26-ancestors.html");
		assertModelNotEmpty();

		assertContains(VCARD.fn, "John Doe");
		assertNotContains(
                null,
                VCARD.fn,
				"Mister Jonathan John Doe-Smith Medical Doctor"
        );
		assertContains(VCARD.nickname, "JJ");
		assertNotContains(RDF.TYPE, VCARD.Address);
		assertContains(VCARD.tz, "-0700");
		assertContains(VCARD.title, "President");
		assertContains(VCARD.role, "Chief");
		assertContains(VCARD.organization_name, "Intellicorp");
		assertContains(VCARD.organization_unit, "Intelligence");

		assertContains(VCARD.tel, RDFHelper.uri("tel:415.555.1234"));
		assertContains(VCARD.uid, "abcdefghijklmnopqrstuvwxyz");
		assertContains(VCARD.note, "this is a note");
		assertContains(VCARD.class_, "public");

		assertNotContains(RDF.TYPE, VCARD.Location);
		assertContains(VCARD.geo, (Resource) null);
		assertNotContains(null, VCARD.latitude, "37.77");
		assertNotContains(null, VCARD.longitude, "-122.41");

		assertContains(RDF.TYPE, VCARD.Name);
		assertContains(VCARD.additional_name, "John");
		assertContains(VCARD.given_name, "Jonathan");
		assertContains(VCARD.family_name, "Doe-Smith");
		assertContains(VCARD.honorific_prefix, "Mister");
		assertContains(VCARD.honorific_suffix, "Medical Doctor");

		assertNotContains(null, VCARD.post_office_box, "Box 1234");
		assertNotContains(null, VCARD.extended_address, "Suite 100");
		assertNotContains(null, VCARD.street_address, "123 Fake Street");
		assertNotContains(null, VCARD.locality, "San Francisco");
		assertNotContains(null, VCARD.region, "California");
		assertNotContains(null, VCARD.postal_code, "12345-6789");
		assertNotContains(null, VCARD.country_name, "United States of America");
		assertNotContains(null, VCARD.addressType, "work");
	}


    @Test
	public void testfnOrg() throws RepositoryException {
        assertExtracts("hcard/30-fn-org.html");
        assertModelNotEmpty();
        assertStatementsSize(RDF.TYPE, VCARD.VCard, 5);
        RepositoryResult<Statement> repositoryResult = conn.getStatements(null, RDF.TYPE, VCARD.VCard, false);
        try {
            while (repositoryResult.hasNext()) {
                Resource card = repositoryResult.next().getSubject();
                Assert.assertNotNull(findObject(card, VCARD.fn));
                String name = findObjectAsLiteral(card, VCARD.fn);

                Assert.assertNotNull(findObject(card, VCARD.org));
                Resource org = findObjectAsResource(card, VCARD.org);
                Assert.assertNotNull(findObject(org, VCARD.organization_name));

                if (name.equals("Dan Connolly")) {
                    Assert.assertNotNull(findObject(card, VCARD.n));
                    Assert.assertFalse(name.equals(org.stringValue()));
                }
            }
        } finally {
            repositoryResult.close();
        }
    }

    @Test
    public void testInclude() throws RepositoryException {
        assertExtracts("hcard/31-include.html");
        assertModelNotEmpty();
        assertStatementsSize(RDF.TYPE, VCARD.VCard, 3);
        assertStatementsSize(VCARD.email, null, 3);

        RepositoryResult<Statement> statements = conn.getStatements(null, RDF.TYPE, VCARD.VCard, false);
        try {
            while (statements.hasNext()) {
                Resource vcard = statements.next().getSubject();

                Assert.assertNotNull(findObject(vcard, VCARD.fn));
                Assert.assertEquals("Brian Suda", findObjectAsLiteral(vcard, VCARD.fn));

                Assert.assertNotNull(findObject(vcard, VCARD.url));
                String url = findObjectAsResource(vcard, VCARD.url).stringValue();
                Assert.assertEquals("http://suda.co.uk/", url);

                Resource name = findObjectAsResource(vcard, VCARD.n);
                Assert.assertEquals(
                        "Brian",
                        findObjectAsLiteral(name, VCARD.given_name)
                );
                Assert.assertEquals(
                        "Suda",
                        findObjectAsLiteral(name, VCARD.family_name)
                );

                //Included data.
                Assert.assertNotNull(findObject(vcard, VCARD.email));
                String mail = findObjectAsLiteral(vcard, VCARD.email);
                Assert.assertEquals("mailto:correct@example.com", mail);
            }
        } finally {
            statements.close();
        }
    }

    @Test
    public void testHeader() throws RepositoryException {
        assertExtracts("hcard/32-header.html");
        assertModelNotEmpty();
        // check fn, name, family, nick.
        assertJohn();

        RepositoryResult<Statement> statements = conn.getStatements(null, RDF.TYPE, VCARD.VCard, false);
        try {
            Resource example = RDFHelper.uri("http://example.org/");
            while (statements.hasNext()) {
                Resource card = statements.next().getSubject();
                Assert.assertNotNull( findObject(card, VCARD.fn) );

                String fn = findObjectAsLiteral(card, VCARD.fn);
                if ("Jane Doe".equals(fn)) {
                    // Assert.assertNotNull( findObject(card, VCARD.url));
                    Assert.assertNotNull( findObject(card, VCARD.org));
                } else {
                    Assert.assertTrue("John Doe".equals(fn) || "Brian Suda".equals(fn));

                    Assert.assertNotNull( findObject(card, VCARD.url));
                    Assert.assertEquals(example, findObjectAsResource(card, VCARD.url));

                    Assert.assertNotNull( findObject(card, VCARD.org) );
                    Resource org = findObjectAsResource(card, VCARD.org);
                    assertContains(org, RDF.TYPE, VCARD.Organization);
                    Assert.assertNotNull(org);
                    Assert.assertNotNull( findObject(card, VCARD.org) );
                    Assert.assertNotNull( findObject(org , VCARD.organization_name) );
                    Assert.assertEquals(
                            "example.org",
                            findObjectAsLiteral(org, VCARD.organization_name)
                    );
                }
            }
            // Just to be sure there are no spurious statements-
            // assertStatementsSize(VCARD.org, null, 2);
            assertStatementsSize(VCARD.url, example, 2);
        } finally {
            statements.close();
        }
    }

    @Test
	public void testAreaFull() throws RepositoryException {
		assertExtracts("hcard/33-area.html");
		assertModelNotEmpty();
		assertStatementsSize(RDF.TYPE, VCARD.VCard, 5);

		RepositoryResult<Statement> statements = conn.getStatements(null, RDF.TYPE, VCARD.VCard, false);
		while (statements.hasNext()) {
			Resource vcard = statements.next().getSubject();
			final Value fnValue = findObject(vcard, VCARD.fn);
            Assert.assertNotNull(fnValue);
			String fn = fnValue.stringValue();
			final Value vcardValue = findObject(vcard, VCARD.url);
            Assert.assertNotNull(vcardValue);
			String url = vcardValue.stringValue();
			final Value emailValue = findObject(vcard, VCARD.email);
            Assert.assertNotNull(emailValue);
			String mail = emailValue.stringValue();
			Assert.assertEquals("Joe Public", fn);
			Assert.assertEquals("http://example.com/", url);
			Assert.assertEquals("mailto:joe@example.com", mail);
		}
	}

    @Test
    public void testCategories() throws RepositoryException {
		assertExtracts("hcard/36-categories.html");
		assertModelNotEmpty();
		assertContains(VCARD.given_name, "Joe");
		assertContains(VCARD.given_name, "john");
		assertContains(VCARD.family_name, "doe");
		assertContains(VCARD.family_name, "User");
		assertContains(VCARD.fn, "john doe");
		assertContains(VCARD.fn, "Joe User");

		String[] cats = {
                "C1", "C2a", "C3", "C4", "C5", "C6", "C7", "C9", "luser", "User", "D1", "D2", "D3"
        };
		for (String cat : cats)
			assertContains(VCARD.category, cat);
		assertNotContains(null, VCARD.category, "D4");
	}

    @Test
    public void testSingleton() throws RepositoryException {
		// this tests probably tests that e just get the first fn and so on
		assertExtracts("hcard/37-singleton.html");
		assertModelNotEmpty();
		assertStatementsSize(VCARD.fn, null, 1);
		assertContains(VCARD.fn, "john doe 1");

		assertStatementsSize(RDF.TYPE, VCARD.Name, 1);
		assertStatementsSize(VCARD.given_name, null, 1);
		assertContains(VCARD.given_name, "john");
		assertStatementsSize(VCARD.family_name, null, 1);
		assertContains(VCARD.family_name, "doe");
		assertStatementsSize(VCARD.sort_string, null, 1);
		assertContains(VCARD.sort_string, "d");

		assertStatementsSize(VCARD.bday, null, 1);
		assertContains(VCARD.bday, "20060707");
		assertStatementsSize(VCARD.rev, null, 1);
		assertContains(VCARD.rev, "20060707");
		assertStatementsSize(VCARD.class_, null, 1);
		assertContains(VCARD.class_, "public");
		assertStatementsSize(VCARD.tz, null, 1);
		assertContains(VCARD.tz, "+0600");

		// Why 0? because the extractor does not look at geo uF!
		assertStatementsSize(RDF.TYPE, VCARD.Location, 0);
		assertStatementsSize(VCARD.geo, null, 2);

		assertNotContains(null, VCARD.latitude, "123.45");
		assertNotContains(null, VCARD.longitude, "67.89");

		assertStatementsSize(VCARD.uid, null, 1);
		assertContains(VCARD.uid, "unique-id-1");
	}

    @Test
    public void testUidFull() throws RepositoryException {
        assertExtracts("hcard/38-uid.html");
        assertModelNotEmpty();
        assertStatementsSize(RDF.TYPE, VCARD.VCard, 4);
        RepositoryResult<Statement> statements = conn.getStatements(null, RDF.TYPE, VCARD.VCard, false);

        try {
            while (statements.hasNext()) {
                Resource vcard = statements.next().getSubject();
                Assert.assertNotNull( findObject(vcard, VCARD.fn) );
                String fn =  findObjectAsLiteral(vcard, VCARD.fn);
                Assert.assertEquals("Ryan King", fn);

                Assert.assertNotNull( findObject(vcard,VCARD.n) );
                Resource n = findObjectAsResource(vcard, VCARD.n);
                Assert.assertNotNull(n);
                Assert.assertNotNull(findObject(n, VCARD.given_name) );
                Assert.assertEquals("Ryan",  findObjectAsLiteral( n, VCARD.given_name) );
                Assert.assertNotNull( findObject(n, VCARD.family_name) );
                Assert.assertEquals("King", findObjectAsLiteral(n, VCARD.family_name) );

                Assert.assertNotNull( findObject(vcard, VCARD.url) );
                Resource url = findObjectAsResource(vcard, VCARD.url);

                Assert.assertNotNull( findObject(vcard, VCARD.uid) );
                String uid = findObjectAsLiteral(vcard, VCARD.uid);

                Assert.assertEquals("http://theryanking.com/contact/", url.stringValue() );
                Assert.assertEquals("http://theryanking.com/contact/", uid);
            }
        } finally {
            statements.close();
        }
    }

    @Test
	public void testRomanianWikipedia() throws RepositoryException {
		assertExtracts("hcard/40-fn-inside-adr.html");
		assertModelNotEmpty();
		assertStatementsSize(RDF.TYPE, VCARD.VCard, 1);
		RepositoryResult<Statement> statements = conn.getStatements(null, RDF.TYPE, VCARD.VCard, false);

        try {
            while (statements.hasNext()) {
                Resource card = statements.next().getSubject();
                Assert.assertNotNull( findObject(card, VCARD.fn) );
                String fn = findObjectAsLiteral( card, VCARD.fn);
                Assert.assertEquals("Berlin", fn);

                Assert.assertNotNull( findObject(card, VCARD.org) );
                Resource org =  findObjectAsResource(card, VCARD.org);
                assertContains(org, RDF.TYPE, VCARD.Organization);
                Assert.assertNotNull(org);
                Assert.assertNotNull( findObject(card, VCARD.org) );
                Assert.assertNotNull( findObject(org, VCARD.organization_name) );
                Assert.assertEquals("Berlin", findObjectAsLiteral(org, VCARD.organization_name));

            }
        } finally {
            statements.close();
        }
    }

    @Test
    public void testNoMicroformats() throws RepositoryException, IOException, ExtractionException {
		extract("/html-without-uf.html");
		assertModelEmpty();
	}

    @Test
    public void testBasic() throws RepositoryException {
        assertExtracts("hcard/01-tantek-basic.html");
        assertModelNotEmpty();
        assertContains(RDF.TYPE, VCARD.VCard);
        // assertContains(RDF.TYPE, VCARD.Organization);
        assertContains(RDF.TYPE, VCARD.Name);
        // assertContains(VCARD.organization_name, "Technorati");
        Resource person = findExactlyOneBlankSubject(
                VCARD.fn,
                RDFHelper.literal("Tantek Celik")
        );
        Assert.assertNotNull(person);
        Resource org = findExactlyOneBlankSubject(
                VCARD.organization_name,
                RDFHelper.literal("Technorati")
        );
        Assert.assertNotNull(org);
        assertContains(
                person,
                VCARD.url,
                RDFHelper.uri("http://tantek.com/")
        );
        assertContains(person, VCARD.n,   (Resource) null);
        assertContains(person, VCARD.org, (Resource) null);
    }

    @Test
    public void testMultipleclassNamesOnVCard() throws RepositoryException {
		assertExtracts("hcard/02-multiple-class-names-on-vcard.html");
		assertModelNotEmpty();
		assertStatementsSize(RDF.TYPE, VCARD.VCard, 4);
		Resource name;
		RepositoryResult<Statement> statements = conn.getStatements(null, RDF.TYPE, VCARD.VCard, false);
		while (statements.hasNext()) {
			name = statements.next().getSubject();
			assertContains(name, VCARD.fn, "Ryan King");
		}
	}

    @Test
	public void testImpliedNames() throws RepositoryException {
		String[] ns = {
                "Ryan King",
                "King",
                "Ryan",

                "Ryan King",
                "King",
				"Ryan",

                "Ryan King",
                "King",
                "Ryan",

                "Brian Suda",
                "Suda",
				"Brian",

                "King, Ryan",
                "King",
                "Ryan",

                "King, R",
                "King",
                "R",

				"King R",
                "R",
                "King",

                "R King",
                "King",
                "R",

                "King R.",
                "R.",
                "King",

                "Jesse James Garrett",
                "Garrett",
                "Jesse",

                "Thomas Vander Wall",
                "Wall",
                "Thomas"
        };
		List<String> NAMES = Arrays.asList(ns);
		assertExtracts("hcard/03-implied-n.html");
		assertModelNotEmpty();
//		assertContains( VCARD.organization_name, "Technorati");

        RepositoryResult<Statement> statements = conn.getStatements(null, VCARD.fn, null, false);
        Resource vcard;
        int count = 0;
        try {
            while (statements.hasNext()) {
                vcard = statements.next().getSubject();
                assertContains(vcard, RDF.TYPE, VCARD.VCard);
                Resource name = findObjectAsResource(vcard, VCARD.n);

                final String objLiteral = findObjectAsLiteral(vcard, VCARD.fn);
                int idx = NAMES.indexOf(objLiteral);
                Assert.assertTrue( String.format("not in names: '%s'", objLiteral), idx >= 0);
                Assert.assertEquals(
                        NAMES.get(idx + 1),
                        findObjectAsLiteral(name, VCARD.family_name)
                );
                Assert.assertEquals(
                        NAMES.get(idx + 2),
                        findObjectAsLiteral(name, VCARD.given_name)
                );
                count++;
            }
        } finally {
            statements.close();
        }
        Assert.assertEquals(10, count);
    }

    @Test
    public void testIgnoreUnknowns() throws RepositoryException {
		assertExtracts("hcard/04-ignore-unknowns.html");
		assertDefaultVCard();
		assertContains(VCARD.fn, "Ryan King");
		assertContains(VCARD.n, (Resource) null);
		assertContains(null, "Ryan");
		assertContains(VCARD.given_name, "Ryan");
		assertContains(VCARD.family_name, "King");
	}

    @Test
    public void testMailto1() throws RepositoryException {
		assertExtracts("hcard/05-mailto-1.html");
		assertDefaultVCard();
		assertContains(VCARD.fn, "Ryan King");
		assertContains(RDF.TYPE, VCARD.Name);

		assertContains(
                VCARD.email,
				RDFHelper.uri("mailto:ryan@technorati.com")
        );

		assertContains(VCARD.given_name , "Ryan");
		assertContains(VCARD.family_name, "King");
	}

    @Test
    public void testMailto2() throws RepositoryException {
		assertExtracts("hcard/06-mailto-2.html");
		assertDefaultVCard();
		assertContains(VCARD.fn, "Brian Suda");

		assertContains(
                VCARD.email,
				RDFHelper.uri("mailto:brian@example.com")
        );
		assertContains(VCARD.given_name, "Brian");
		assertContains(VCARD.family_name, "Suda");
	}

    @Test
    public void testRelativeUrl() throws RepositoryException {
		assertExtracts("hcard/07-relative-url.html");
		assertDefaultVCard();
		assertJohn();
		assertContains( VCARD.url, RDFHelper.uri(baseURI + "home/blah") );
	}

    @Test
    public void testRelativeUrlBase() throws RepositoryException {
		assertExtracts("hcard/08-relative-url-base.html");
		assertDefaultVCard();
		assertContains(VCARD.url, RDFHelper.uri(baseURI + "home/blah"));
		assertJohn();
	}

    @Test
    public void testRelativeUrlXmlBase1() throws RepositoryException {
		assertExtracts("hcard/09-relative-url-xmlbase-1.html");
		assertDefaultVCard();
		assertContains(VCARD.url, RDFHelper.uri((baseURI + "home/blah")));
		assertJohn();
	}

    @Test
    public void testRelativeUrlXmlBase2() throws RepositoryException {
		assertExtracts("hcard/10-relative-url-xmlbase-2.html");
		assertDefaultVCard();
		assertContains(VCARD.url, RDFHelper.uri((baseURI + "home/blah")));
		assertJohn();
	}

    @Test
    public void testMultipleUrls() throws RepositoryException {
		assertExtracts("hcard/11-multiple-urls.html");
		assertDefaultVCard();
		assertContains(VCARD.url, RDFHelper.uri(("http://example.com/foo")));
		assertContains(VCARD.url, RDFHelper.uri(("http://example.com/bar")));

		assertJohn();
	}

    @Test
    public void testImageSrc() throws RepositoryException {
		assertExtracts("hcard/12-img-src-url.html");
		assertDefaultVCard();
		assertJohn();
	}

    @Test
    public void testPhotoLogo() throws RepositoryException {
		assertExtracts("hcard/13-photo-logo.html");
		assertDefaultVCard();
		assertContains(VCARD.photo, RDFHelper.uri(("http://example.org/picture.png")));
		assertContains(VCARD.logo , RDFHelper.uri(("http://example.org/picture.png")));
		assertJohn();
	}

    @Test
    public void testImgSrcDataUrl() throws RepositoryException {
		assertExtracts("hcard/14-img-src-data-url.html");
		assertDefaultVCard();
		Resource data = RDFHelper.uri(
                          "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAMAAAAp4XiDAAAABGdBTUEAAK/"
						+ "INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAASUExURf///8zMzJmZmWZmZ"
						+ "jMzMwAAAPOPemkAAAM1SURBVHjaYmBgYGBkYQUBFkYWFiCPCchixQAMCCZAACF0MAMVM4K4TFh0IGsBCC"
						+ "AkOxhYmBnAAKaHhZkZmxaAAGJgYIbpYGBihGgBWsTMzMwE4jIhaWGAYoAAYmCECDExYAcwGxkg5oNIgAB"
						+ "igDqLARdgZmGB2wICrKwAAcSA3xKgIxlZ0PwCEEAMBCxhgHoWSQtAADFAAxgfYEJ1GEAAQbQw4tUCsocB"
						+ "YQVAADEgu4uRkREeUCwszEwwLhOKLQABhNDCBA4aSDgwwhIAJKqYUPwCEEAMUK/AUwnc9aywJMCI7DAgA"
						+ "AggBohZ8JTBhGIJzCoWZL8ABBCYidAB8RUjWppkYUG2BSCAGMDqEMZiswUtXgACiAHsFYixTMywGGLGpgU"
						+ "WYgABxAA2mQkWCMyMqFoYmdD8ACQAAogBHJHMrCxg1cyIiICmCkYWDFsAAgiihYmZCewFFpR0BfI3LLch+"
						+ "QUggBiQ0iQjEyMDmh54qCBlUIAAYsCRJsElADQvgWKTlRGeKwECiAF3XgGmMEYQYADZzcoA9z5AAMG9RQC"
						+ "AtEC9DxBADFiyFyMjVi0wABBAWLQwQdIiuhYGWJIACCBg+KKUJ9BoBRdS2LQALQMIIGDQIEmwAO1kYcVWH"
						+ "CDZAhBAqFqYmOAxj2YNtAwDAYAAYmDEiBYWzHKKkRERYiwAAYSphZEZwxZGZiZQVEJTJkAAMTCyokc7M5o"
						+ "ORlC5wcoEjxeAAAJqQXU0UB6W5WFmABMtEzMi1wEEEFAbE0YyAUuzMMEsYQalMkQSBQggUDmNPU3C9IA4L"
						+ "CxI+QUggEBiKOU8yExgqccCL3chnkPKlQABhGo6ejHBDKmdUHMlQAAhhQvQaGZGkBIkjcAMywLmI+VKgAB"
						+ "CSowsTJhZkhlWXiBpAQggYBqBZl9GVOdBcz0LZqEEEEAMqLULMBLg1THWog9IAwQQA0qiZcRW5aPbAhBAD"
						+ "Cg1El4tMAAQQAxoiZYZXnTh1AIQQAzo2QlYpDDjcBgrxGEAAcSAJTthswmiBUwDBBC2GpkZJTaRvQ+mAQK"
						+ "IAUuuxdZWQvILQABBmSxMjBj5EpcWgACCMoFOYYSpZyHQHgMIMACt2hmoVEikCQAAAABJRU5ErkJggg=="
        );

		assertContains(VCARD.photo, data);
		assertContains(VCARD.logo, data);
		assertJohn();
	}

    @Test
    public void testHonorificAdditionalSingle() throws RepositoryException {
		assertExtracts("hcard/15-honorific-additional-single.html");
		assertDefaultVCard();
		assertContains(VCARD.fn, "Mr. John Maurice Doe, Ph.D.");

		assertContains(VCARD.honorific_prefix, "Mr.");
		assertContains(VCARD.honorific_suffix, "Ph.D.");

		assertContains(VCARD.given_name, "John");
		assertContains(VCARD.additional_name, "Maurice");
		assertContains(VCARD.family_name, "Doe");
	}

    @Test
    public void testHonorificAdditionalMultiple() throws RepositoryException {
		assertExtracts("hcard/16-honorific-additional-multiple.html");
		assertDefaultVCard();
		assertContains(VCARD.honorific_prefix, "Mr.");
		assertContains(VCARD.honorific_prefix, "Dr.");

		assertContains(VCARD.honorific_suffix, "Ph.D.");
		assertContains(VCARD.honorific_suffix, "J.D.");

		assertContains(VCARD.given_name, "John");
		assertContains(VCARD.additional_name, "Maurice");
		assertContains(VCARD.additional_name, "Benjamin");
		assertContains(VCARD.family_name, "Doe");

		assertContains(
                VCARD.fn,
				"Mr. Dr. John Maurice Benjamin Doe Ph.D., J.D."
        );
	}

    @Test
    public void testEMailNotUri() throws RepositoryException {
		assertExtracts("hcard/17-email-not-uri.html");
		assertDefaultVCard();
		assertJohn();
		assertContains( VCARD.email, RDFHelper.uri("mailto:john@example.com") );
	}

	@Test
    public void testObjectDataHttpUri() throws RepositoryException {
		assertExtracts("hcard/18-object-data-http-uri.html");
		assertDefaultVCard();
		assertJohn();
	}

    @Test
    public void testObjectDataDataUri() throws RepositoryException {
		assertExtracts("hcard/19-object-data-data-uri.html");
		assertDefaultVCard();
        assertJohn();

		assertContains(VCARD.photo, (Resource) null);
		assertContains(VCARD.logo , (Resource) null);
	}

    @Test
    public void testImgAlt() throws RepositoryException {
		assertExtracts("hcard/20-image-alt.html");
		assertDefaultVCard();
		Resource uri = RDFHelper.uri("http://example.com/foo.png");
		assertContains(VCARD.photo, uri);
		assertContains(VCARD.logo, uri);
		assertJohn();
	}

    @Test
    public void testAdr() throws RepositoryException {
		assertExtracts("hcard/22-adr.html");
		assertDefaultVCard();
		assertJohn();
		assertStatementsSize(RDF.TYPE, VCARD.Address, 0);
	}

    @Test
    public void testBirthDayDate() throws RepositoryException {
		assertExtracts("hcard/27-bday-date.html");
		assertModelNotEmpty();
		assertContains(VCARD.fn         , "john doe");
		assertContains(VCARD.given_name , "john");
		assertContains(VCARD.family_name, "doe");
		assertContains(VCARD.bday       , "2000-01-01");
	}

    @Test
    public void testBirthDayDateTime() throws RepositoryException {
		assertExtracts("hcard/28-bday-datetime.html");
		assertModelNotEmpty();
		assertContains(VCARD.fn         , "john doe");
		assertContains(VCARD.given_name , "john");
		assertContains(VCARD.family_name, "doe");
		assertContains(VCARD.bday       , "2000-01-01T00:00:00");
	}

    @Test
    public void testBirthDayDateTimeTimeZone() throws RepositoryException {
		assertExtracts("hcard/29-bday-datetime-timezone.html");
		assertModelNotEmpty();
		assertContains(VCARD.fn, "john doe");
		assertContains(VCARD.given_name, "john");
		assertContains(VCARD.family_name, "doe");
		assertContains(VCARD.bday, "2000-01-01T00:00:00-0800");
	}

    // @Test
    public void testArea() throws RepositoryException {
        assertExtracts("hcard/33-area.html");
        assertModelNotEmpty();
        assertStatementsSize(RDF.TYPE, VCARD.VCard, 5);
        RepositoryResult<Statement> statements = conn.getStatements(null, RDF.TYPE, VCARD.VCard, false);
        try {
            while (statements.hasNext()) {
                Resource vcard = statements.next().getSubject();

                Assert.assertNotNull(findObject(vcard, VCARD.fn));
                Assert.assertEquals("Joe Public", findObjectAsLiteral(vcard, VCARD.fn));
                Assert.assertNotNull(findObject(vcard, VCARD.url));
                String url = findObjectAsLiteral(vcard, VCARD.url);
                Assert.assertNotNull(findObject(vcard, VCARD.email));
                String mail = findObjectAsLiteral(vcard, VCARD.email);
                Assert.assertEquals("http://example.com/", url);
                Assert.assertEquals("mailto:joe@example.com", mail);
            }
        } finally {
            statements.close();
        }

        // Check that there are 4 organizations.
        assertStatementsSize(RDF.TYPE, VCARD.Organization, 4);
        statements = conn.getStatements(null, RDF.TYPE, VCARD.Organization, false);
        try {
            while (statements.hasNext()) {
                Resource org = statements.next().getSubject();
                assertContains(null, VCARD.org, org);
                Assert.assertNotNull( findObject(org, VCARD.organization_name) );
                Assert.assertEquals("Joe Public", findObjectAsLiteral(org, VCARD.organization_name) );
            }
        } finally {
            statements.close();
        }
    }

    /*
    public void testNotes() {
		assertExtracts("34-notes");
		assertModelNotEmpty();
		StmtIterator iter = model.listStatements(null, RDF.type, VCARD.VCard);
		//		dumpModel();
		while (iter.hasNext()) {
			Resource vcard =  iter.nextStatement().getSubject();
			String fn = vcard.getProperty(VCARD.fn).getString();
			String mail = vcard.getProperty(VCARD.email).getResource().getURI();
			Assert.assertEquals("Joe Public", fn);

			Assert.assertEquals("mailto:joe@example.com", mail);
		}
		// TODO: reactivate
		//		assertContains(VCARD.note, "Note 1Note 3Note 4 with a ; and a , to be escaped");
	}
	*/

    /*
    public void testIncludePattern() {
		assertExtracts("35-include-pattern");
		assertModelNotEmpty();
		assertStatementsSize(RDF.type, VCARD.VCard, 3);

		ResIterator iter = model.listSubjectsWithProperty(RDF.type, VCARD.Name);
		while (iter.hasNext()) {
			Resource name =  iter.nextResource();
			Assert.assertTrue(name.hasProperty(VCARD.given_name));
			String gn = name.getProperty(VCARD.given_name).getString();
			Assert.assertEquals("James", gn);
			Assert.assertTrue(name.hasProperty(VCARD.family_name));
			String fn = name.getProperty(VCARD.family_name).getString();
			Assert.assertEquals("Levine", fn);
		}
		iter.close();

		assertStatementsSize(RDF.type, VCARD.Organization, 2);
		iter = model.listSubjectsWithProperty(RDF.type, VCARD.Organization);
		while (iter.hasNext()) {
			Resource org = iter.nextResource();
			Assert.assertTrue(org.hasProperty(VCARD.organization_name));
			Assert.assertEquals("SimplyHired",org.getProperty(VCARD.organization_name).getString());

			ResIterator ri = model.listSubjectsWithProperty(VCARD.org, org);
			while(ri.hasNext()) {
				Resource vcard = ri.nextResource();
				Assert.assertTrue(vcard.hasProperty(VCARD.title));
				Assert.assertEquals("Microformat Brainstormer",vcard.getProperty(VCARD.title).getString());
			}
			ri.close();

		}

	}
	*/

    /*
    public void testUid() {
		assertExtracts("38-uid");
		assertModelNotEmpty();
		assertStatementsSize(RDF.type, VCARD.VCard, 4);
		StmtIterator iter = model.listStatements(null, RDF.type, VCARD.VCard);
		while (iter.hasNext()) {
			Resource vcard = (Resource) iter.nextStatement().getSubject();
			Assert.assertTrue(vcard.hasProperty(VCARD.fn));
			String fn = vcard.getProperty(VCARD.fn).getString();
			Assert.assertTrue(vcard.hasProperty(VCARD.url));
			String url = vcard.getProperty(VCARD.url).getResource().getURI();
			Assert.assertTrue(vcard.hasProperty(VCARD.uid));
			String uid = vcard.getProperty(VCARD.uid).getString();
			Assert.assertEquals("Ryan King", fn);
			Assert.assertEquals("http://theryanking.com/contact/",url);
			Assert.assertEquals("http://theryanking.com/contact/", uid);

		}
	}
	*/

    @Test
    public void testIgnoreChildren() throws RepositoryException {
		assertExtracts("hcard/41-ignore-children.html");
		assertModelNotEmpty();
		assertStatementsSize(RDF.TYPE, VCARD.VCard, 1);
		assertContains(VCARD.fn, "Melanie Kl\u00f6\u00df");
		assertContains(VCARD.email, RDFHelper.uri("mailto:mkloes@gmail.com"));
		assertContains(VCARD.adr,(Resource) null);
		assertNotContains(null, VCARD.postal_code,"53127");
		assertNotContains(null, VCARD.locality,"Bonn");
		assertNotContains(null, VCARD.street_address,"Ippendorfer Weg. 24");
		assertNotContains(null, VCARD.country_name,"Germany");
	}

	private void assertDefaultVCard() throws RepositoryException {
		assertModelNotEmpty();
		assertStatementsSize(RDF.TYPE, VCARD.VCard, 1);
	}

    private void assertJohn() throws RepositoryException {
		assertContains(VCARD.fn, "John Doe");
		assertContains(VCARD.given_name, "John");
		assertContains(VCARD.family_name, "Doe");
	}

}