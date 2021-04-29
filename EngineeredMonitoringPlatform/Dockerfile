FROM xresch/cfw-server:latest

WORKDIR /usr/src/cfw-server/
COPY ./resources/ resources/

#COPY ./config config/
# Change Config File path to /data/mount
#RUN sed -i 's/.\/config/\/data\/config/g' ${BASEDIR}/config/cfw.properties
# Change Datastore File path
#RUN sed -i 's/cfw_h2_path=.\/datastore/cfw_h2_path=\/data\/datastore/g' ${BASEDIR}/config/cfw.properties
# Change Log File path
#RUN sed -i 's/.\/log\//\/data\/log\//g' ${BASEDIR}/config/logging.properties

COPY ./target/lib extensions/
COPY ./target/emp-?.?.?.jar extensions/

