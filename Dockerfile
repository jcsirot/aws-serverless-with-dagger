FROM --platform=linux/amd64 amazon/aws-lambda-java:17

# Copy function code and runtime dependencies from Maven layout
COPY translate/target/dependency/* ${LAMBDA_TASK_ROOT}/lib/
COPY translate/target/classes ${LAMBDA_TASK_ROOT}

# Set the handler call name
ARG HANDLER_CLASS
ENV HANDLER_CLASS=${HANDLER_CLASS}

# Set the ENTRYPOINT to run the handler
ENTRYPOINT /lambda-entrypoint.sh "${HANDLER_CLASS}::handleRequest"
