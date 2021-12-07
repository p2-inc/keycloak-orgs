import { useState, useMemo } from "react";

export const useImageModal = () => {
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalImageSrc, setModalImageSrc] = useState("");

    const handlers = useMemo (
        () => ({
            onImageClick: (imageSrc: any) => {
                setModalImageSrc(imageSrc);
                setIsModalOpen(true);
              }
        }),
        []
    )
   

  return [isModalOpen, modalImageSrc, handlers, setIsModalOpen] as const;
};


