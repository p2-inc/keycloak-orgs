import React, { FC } from "react";
import { Card, CardBody, CardTitle } from "@patternfly/react-core";

type Props = {
  title?: string;
};

export const UrlCard: FC<Props> = ({ children }) => (
  <Card isFlat>
    <CardBody>{children}</CardBody>
  </Card>
);
