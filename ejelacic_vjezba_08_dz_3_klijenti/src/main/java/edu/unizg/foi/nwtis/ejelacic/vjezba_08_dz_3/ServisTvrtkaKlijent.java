package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "klijentTvrtka")
@Path("api/tvrtka")
public interface ServisTvrtkaKlijent {
  @HEAD
  public Response headPosluzitelj();

  @Path("status/{id}")
  @HEAD
  public Response headPosluziteljStatus(@PathParam("id") int id);

  @Path("pauza/{id}")
  @HEAD
  public Response headPosluziteljPauza(@PathParam("id") int id);

  @Path("start/{id}")
  @HEAD
  public Response headPosluziteljStart(@PathParam("id") int id);

  @Path("kraj")
  @HEAD
  public Response headPosluziteljKraj();

  @GET
  @Path("obracun")
  @Produces({MediaType.APPLICATION_JSON})
  Response dohvatiObracune(@QueryParam("od") long od, @QueryParam("do") long kraj);

  @GET
  @Path("obracun/jelo")
  @Produces({MediaType.APPLICATION_JSON})
  Response dohvatiObracuneJela(@QueryParam("od") String od, @QueryParam("do") String kraj);

  @GET
  @Path("obracun/pice")
  @Produces({MediaType.APPLICATION_JSON})
  Response dohvatiObracunePica(@QueryParam("od") String od, @QueryParam("do") String kraj);

  @GET
  @Path("obracun/{id}")
  @Produces({MediaType.APPLICATION_JSON})
  Response dohvatiObracunePartnera(@PathParam("id") int id, @QueryParam("od") String od,
      @QueryParam("do") String kraj);

  @Path("kraj/info")
  @HEAD
  public Response headPosluziteljKrajInfo();

  @Path("partner")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getPartneri();

  @Path("partner/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getPartner(@PathParam("id") int id);

  @Path("spavanje")
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  public Response dretvaSpava(@QueryParam("vrijeme") long trajanje);

  @POST
  @Path("partner")
  @Consumes(MediaType.APPLICATION_JSON)
  Response spremiPartnera(edu.unizg.foi.nwtis.podaci.Partner partner);

}
