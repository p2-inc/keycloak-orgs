import MainContentArea from "@/components/layouts/main-content-area";
import PrimaryContentArea from "@/components/layouts/primary-content-area";
import TopHeader from "@/components/navs/top-header";

export default function NotAuthorized() {
  return (
    <>
      <TopHeader header="Not Authorized" />
      <MainContentArea>
        <PrimaryContentArea>
          <h1>You are not authorized to take this action.</h1>
          <h1>Please use the menu to navigate to another page.</h1>
        </PrimaryContentArea>
      </MainContentArea>
    </>
  );
}
