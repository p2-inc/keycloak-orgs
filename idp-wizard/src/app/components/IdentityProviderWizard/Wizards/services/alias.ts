import { Protocols, Providers } from "@app/configurations";
import { generateId } from "@app/utils/generate-id";

type Props = { provider: Providers; protocol: Protocols; preface: string };
type ClearProps = { provider: Providers; protocol: Protocols };

export function getAlias({ provider, protocol, preface }: Props) {
  try {
    const alias = localStorage.getItem(`p2_${provider}_${protocol}`);
    if (alias) {
      return alias;
    }
    return setAlias({ provider, protocol, preface });
  } catch (error) {
    return `${preface}-${generateId()}`;
  }
}

function setAlias({ provider, protocol, preface }: Props) {
  const alias = `${preface}-${generateId()}`;
  localStorage.setItem(`p2_${provider}_${protocol}`, alias);
  return alias;
}

export async function clearAlias({ provider, protocol }: ClearProps) {
  await localStorage.removeItem(`p2_${provider}_${protocol}`);
}
