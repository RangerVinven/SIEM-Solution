rootProject.name = "siem-backend"

include("services:account-service")
include("services:aggregation-service")
include("services:normalisation-service")
include("services:log-saving-service")
include("services:log-query-service")
include("services:log-analysis-service")
include("services:notification-service")
include("services:alert-service")
include("services:agent-service")
include("common:shared-models")
include("common:shared-utils")
include("common:shared-filters")
