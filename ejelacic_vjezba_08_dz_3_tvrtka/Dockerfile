FROM ubuntu:latest

COPY ./target/maven-jlink/default/ /usr/app/

WORKDIR /usr/app/podaci

EXPOSE 8000
EXPOSE 8001
EXPOSE 8002

CMD ["/usr/app/bin/java", "-m", "ejelacic_vjezba_08_dz_3_tvrtka/edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.PosluziteljTvrtka", "NWTiS_04_tvrtka.txt"]
