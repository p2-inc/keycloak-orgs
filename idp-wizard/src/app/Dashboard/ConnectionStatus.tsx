import {
  Card,
  CardBody,
  CardTitle,
  HelperText,
  HelperTextItem,
  Spinner,
  Title,
} from "@patternfly/react-core";
import React, { useEffect, useState } from "react";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import {
  TableComposable,
  Tbody,
  Td,
  Th,
  Thead,
  Tr,
} from "@patternfly/react-table";
import { toUpper } from "lodash";

type idpEntry = {
  alias: string;
  displayName?: string;
  status: boolean;
  protocol: string;
};

export const ConnectionStatus = () => {
  const [idps, setIdps] = useState<idpEntry[] | []>([]);
  const [loading, setLoading] = useState(true);

  const getConnectionStatus = async () => {
    const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm] =
      useKeycloakAdminApi();
    const realm = getRealm()!;

    await setKcAdminClientAccessToken();

    const idps = await kcAdminClient.identityProviders.find({ realm });

    setIdps(
      idps.map((i) => ({
        alias: i.alias,
        displayName: i.displayName,
        status: i.enabled,
        protocol: i.providerId,
      }))
    );

    setLoading(false);
  };

  useEffect(() => {
    getConnectionStatus();
  }, []);

  return (
    <Card className="card-shadow">
      <CardTitle className="pf-u-pb-0">
        <div className="pf-u-display-flex pf-u-justify-content-flex-start pf-u-align-items-center">
          <Title headingLevel="h2" size="xl">
            Connection Status
          </Title>
          {loading && <Spinner isSVG size="lg" className="pf-u-ml-md" />}
        </div>
      </CardTitle>
      <CardBody>
        <TableComposable
          aria-label="Connection table"
          variant="compact"
          borders={false}
        >
          <Thead>
            <Tr>
              <Th>Connection</Th>
              <Th>Protocol</Th>
              <Th>Status</Th>
            </Tr>
          </Thead>
          <Tbody>
            {idps.map((idp, ind) => (
              <Tr key={ind}>
                <Td dataLabel={idp.alias}>{idp.displayName || idp.alias}</Td>
                <Td dataLabel={idp.protocol}>{toUpper(idp.protocol)}</Td>
                <Td dataLabel={idp.status}>
                  <HelperText>
                    <HelperTextItem
                      variant={idp.status ? "success" : "default"}
                      hasIcon
                    >
                      {idp.status ? "connected" : "disabled"}
                    </HelperTextItem>
                  </HelperText>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </TableComposable>
      </CardBody>
    </Card>
  );
};
