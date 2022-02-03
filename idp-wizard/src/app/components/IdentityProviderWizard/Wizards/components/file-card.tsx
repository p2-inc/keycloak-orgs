import React, { FC } from "react";
import { Card, CardBody, CardTitle } from "@patternfly/react-core";

type Props = {
  title?: string;
};

export const FileCard: FC<Props> = ({
  title = "Upload metadata file",
  children,
}) => (
  <Card isFlat>
    <CardTitle>{title}</CardTitle>
    <CardBody>{children}</CardBody>
  </Card>
);
