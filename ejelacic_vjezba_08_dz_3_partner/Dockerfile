FROM ubuntu:latest

COPY ./target/maven-jlink/default/ /usr/app/

WORKDIR /usr/app/podaci

EXPOSE 8010
EXPOSE 8011

CMD ["sh", "-c", "sleep 1 && /usr/app/bin/java -m ejelacic_vjezba_08_dz_3_partner/edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.PosluziteljPartner NWTiS_04_partner_1.txt"]
#CMD ["/usr/app/bin/java", "-m", "ejelacic_vjezba_08_dz_3_partner/edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.PosluziteljPartner", "NWTiS_04_partner_1.txt"]
#CMD ["/bin/bash"]
