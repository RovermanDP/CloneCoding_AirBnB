import { PropertyDetailWorkspace } from "@/features/properties/components/property-detail-workspace";

export default async function PropertyDetailPage({
  params
}: {
  params: Promise<{ propertyId: string }>;
}) {
  const { propertyId } = await params;
  return <PropertyDetailWorkspace propertyId={propertyId} />;
}
